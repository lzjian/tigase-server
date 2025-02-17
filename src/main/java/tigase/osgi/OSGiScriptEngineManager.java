/**
 * Tigase XMPP Server - The instant messaging server
 * Copyright (C) 2004 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class acts as a delegate for all the available ScriptEngineManagers. Unluckily, the standard did not define it
 * as an interface, so we need to extend it to allow polymorphism. However, no calls to super are used. It wraps all
 * available ScriptEngineManagers in the OSGi ServicePlatform into a merged ScriptEngineManager.
 * <br>
 * Internally, what this class does is creating ScriptEngineManagers for each bundle that contains a ScriptEngineFactory
 * and includes a META-INF/services/javax.script.ScriptEngineFactory file. It assumes that the file contains a list of
 * {@link} ScriptEngineFactory classes. For each bundle, it creates a ScriptEngineManager, then merges them. @link
 * ScriptEngineFactory objects are wrapped into @link OSGiScriptEngineFactory objects to deal with problems of context
 * class loader: Those scripting engines that rely on the ContextClassloader for finding resources need to use this
 * wrapper and the @link OSGiScriptFactory. Mainly, jruby does.
 * <br>
 * Note that even if no context classloader issues arose, it would still be needed to search manually for the factories
 * and either use them directly (losing the mimeType/extension/shortName mechanisms for finding engines or manually
 * registering them) or still use this class, which would be smarter. In the latter case, it would only be needed to
 * remove the hack that temporarily sets the context classloader to the appropriate, bundle-related, class loader.
 * <br>
 * Caveats: <ul><li> All factories are wrapped with an {@link OSGiScriptEngineFactory}. As Engines are not wrapped,
 * calls like <code> ScriptEngineManager osgiManager=new OSGiScriptEngineManager(context);<br> ScriptEngine
 * engine=osgiManager.getEngineByName("ruby"); ScriptEngineFactory factory=engine.getFactory() //this does not return
 * the OSGiFactory wrapper factory.getScriptEngine(); //this might fail, as it does not use OSGiScriptEngineFactory
 * wrapper </code> might result in unexpected errors. Future versions may wrap the ScriptEngine with a OSGiScriptEngine
 * to solve this issue, but for the moment it is not needed. </li> </ul>
 */
public class OSGiScriptEngineManager
		extends ScriptEngineManager {

	private static final Logger log = Logger.getLogger(OSGiScriptEngineManager.class.getCanonicalName());

	private Bindings bindings;
	private Map<ScriptEngineManager, ClassLoader> classLoaders;
	private BundleContext context;

	public OSGiScriptEngineManager() {
		this(Activator.getBundle().getBundleContext());
	}

	public OSGiScriptEngineManager(BundleContext context) {
		this.context = context;
		bindings = new SimpleBindings();
		this.classLoaders = findManagers(context);
	}

	/**
	 * This method is the only one that is visible and not part of the ScriptEngineManager class. Its purpose is to find
	 * new managers that weren't available before, but keeping the globalScope bindings set. If you want to clean the
	 * bindings you can either get a fresh instance of OSGiScriptManager or setting up a new bindings object. This can
	 * be done with: <code> ScriptEngineManager manager=new OSGiScriptEngineManager(context); (...)//do stuff
	 * osgiManager=(OSGiScriptEngineManager)manager;//cast to ease reading osgiManager.reloadManagers();
	 * <br>
	 * manager.setBindings(new OSGiBindings());//or you can use your own bindings implementation
	 * <br>
	 * </code>
	 */
	public void reloadManagers() {
		this.classLoaders = findManagers(context);
	}

	public Object get(String key) {
		return bindings.get(key);
	}

	public Bindings getBindings() {
		return bindings;
	}

	/**
	 * Follows the same behavior of @link javax.script.ScriptEngineManager#setBindings(Bindings) This means that the
	 * same bindings are applied to all the underlying managers.
	 *
	 */
	public void setBindings(Bindings bindings) {
		this.bindings = bindings;
		for (ScriptEngineManager manager : classLoaders.keySet()) {
			manager.setBindings(bindings);
		}

	}

	public ScriptEngine getEngineByExtension(String extension) {
		//TODO this is a hack to deal with context class loader issues
		ScriptEngine engine = null;
		for (ScriptEngineManager manager : classLoaders.keySet()) {
			ClassLoader old = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(classLoaders.get(manager));
			engine = manager.getEngineByExtension(extension);
			Thread.currentThread().setContextClassLoader(old);
			if (engine != null) {
				break;
			}
		}
		return engine;
	}

	public ScriptEngine getEngineByMimeType(String mimeType) {
		//TODO this is a hack to deal with context class loader issues
		ScriptEngine engine = null;
		for (ScriptEngineManager manager : classLoaders.keySet()) {
			ClassLoader old = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(classLoaders.get(manager));
			engine = manager.getEngineByMimeType(mimeType);
			Thread.currentThread().setContextClassLoader(old);
			if (engine != null) {
				break;
			}
		}
		return engine;
	}

	public ScriptEngine getEngineByName(String shortName) {
		//TODO this is a hack to deal with context class loader issues
		for (ScriptEngineManager manager : classLoaders.keySet()) {
			ClassLoader old = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(classLoaders.get(manager));
			ScriptEngine engine = manager.getEngineByName(shortName);
			Thread.currentThread().setContextClassLoader(old);
			if (engine != null) {
				return new OSGiScriptEngine(engine, new OSGiScriptEngineFactory(engine.getFactory(),
																				classLoaders.get(manager)));
			}
		}
		return null;
	}

	public List<ScriptEngineFactory> getEngineFactories() {
		List<ScriptEngineFactory> osgiFactories = new ArrayList<ScriptEngineFactory>();
		for (ScriptEngineManager engineManager : classLoaders.keySet()) {
			for (ScriptEngineFactory factory : engineManager.getEngineFactories()) {
				osgiFactories.add(new OSGiScriptEngineFactory(factory, classLoaders.get(engineManager)));
			}
		}
		return osgiFactories;
	}

	public void put(String key, Object value) {
		bindings.put(key, value);
	}

	public void registerEngineExtension(String extension, ScriptEngineFactory factory) {
		for (ScriptEngineManager engineManager : classLoaders.keySet()) {
			engineManager.registerEngineExtension(extension, factory);
		}
	}

	public void registerEngineMimeType(String type, ScriptEngineFactory factory) {
		for (ScriptEngineManager engineManager : classLoaders.keySet()) {
			engineManager.registerEngineMimeType(type, factory);
		}
	}

	public void registerEngineName(String name, ScriptEngineFactory factory) {
		for (ScriptEngineManager engineManager : classLoaders.keySet()) {
			engineManager.registerEngineName(name, factory);
		}
	}

	private Map<ScriptEngineManager, ClassLoader> findManagers(BundleContext context) {
		Map<ScriptEngineManager, ClassLoader> managers = new HashMap<ScriptEngineManager, ClassLoader>();
		try {
			for (String factoryName : findFactoryCandidates(context)) {
				//We do not really need the class, but we need the classloader 
				ClassLoader factoryLoader = Class.forName(factoryName).getClassLoader();
				factoryLoader = new CustomClassLoader(
						new ClassLoader[]{factoryLoader, this.getClass().getClassLoader()}, factoryLoader);
				ScriptEngineManager manager = new ScriptEngineManager(factoryLoader);
				manager.setBindings(bindings);
				managers.put(manager, factoryLoader);
			}
			return managers;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException(cnfe);
		}
	}

	/**
	 * Iterates through all bundles to get the available @link ScriptEngineFactory classes
	 *
	 * @return the names of the available ScriptEngineFactory classes
	 *
	 */
	private List<String> findFactoryCandidates(BundleContext context) throws IOException {
		Bundle[] bundles = context.getBundles();
		List<String> factoryCandidates = new ArrayList<String>();
		for (Bundle bundle : bundles) {
			if (log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "checking bundle for script engine manager - Id: {0} Bundle: {1}  Version: {2}",
						new Object[]{bundle.getBundleId(), bundle.getSymbolicName(), bundle.getVersion().toString()});
			}
			if (bundle.getSymbolicName() == null || bundle.getSymbolicName().equals("system.bundle")) {
				continue;
			}
			Enumeration urls = bundle.findEntries("META-INF/services", "javax.script.ScriptEngineFactory", false);
			if (urls == null) {
				continue;
			}
			while (urls.hasMoreElements()) {
				URL u = (URL) urls.nextElement();
				BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					factoryCandidates.add(line.trim());
				}
			}
		}
		return factoryCandidates;
	}

	private class CustomClassLoader
			extends ClassLoader {

		private final ClassLoader[] loaders;

		public CustomClassLoader(ClassLoader[] loaders, ClassLoader mainLoader) {
			super(mainLoader);
			this.loaders = loaders;
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return findClass(name);
		}

		@Override
		public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			return findClass(name);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			Class<?> cls = null;
			for (ClassLoader cl : loaders) {
				try {
					cls = cl.loadClass(name);
					if (cls != null) {
						return cls;
					}
				} catch (Exception ex) {
				}
			}

			if (cls == null) {
				return findSystemClass(name);
			}

			return cls;
		}

	}
}