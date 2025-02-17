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
package tigase.disco;

import tigase.xml.Element;
import tigase.xmpp.impl.PresenceCapabilitiesManager;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tigase.xmpp.impl.PresenceCapabilitiesManager.CAPS_NODE;
import static tigase.xmpp.impl.PresenceCapabilitiesManager.generateVerificationString;

/**
 * Describe class ServiceEntity here.
 * <br>
 * Created: Sat Feb 10 13:11:34 2007
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/
public class ServiceEntity {

	private static Logger log = Logger.getLogger(ServiceEntity.class.getName());

	private boolean adminOnly = false;
	private Function<String, Element> extensionSupplier;
	private Set<String> features = null;
	private List<ServiceIdentity> identities = null;
	private Set<ServiceEntity> items = null;
	private String jid = null;
	private String name = null;
	private String node = null;

	public ServiceEntity(String jid, String node, String name) {
		this(jid, node, name, null);
	}
	
	public ServiceEntity(String jid, String node, String name, Function<String, Element> extensionSupplier) {
		this.jid = jid;
		this.node = node;
		this.name = name;
		this.extensionSupplier = extensionSupplier;
	}

	public ServiceEntity(String jid, String node, String name, Function<String, Element> extensionSupplier, boolean adminOnly) {
		this.jid = jid;
		this.node = node;
		this.name = name;
		this.extensionSupplier = extensionSupplier;
		this.adminOnly = adminOnly;
	}

//	public Element getExtensions() {
//		return extensions.clone();
//	}
//
//	public void setExtensions(Element extensions) {
//		this.extensions = extensions;
//	}

	public void addFeatures(String... features) {
		if (this.features == null) {
			this.features = new LinkedHashSet<String>();
		}
		Collections.addAll(this.features, features);
	}

	public void addIdentities(ServiceIdentity... identities) {
		if (this.identities == null) {
			this.identities = new ArrayList<ServiceIdentity>();
		}
		Collections.addAll(this.identities, identities);
	}

	public void addItems(ServiceEntity... items) {
		if (this.items == null) {
			this.items = new LinkedHashSet<ServiceEntity>();
		}

		// It may look very strange but look at equals() method....
		// So some items which might be the same from the Set point of
		// view are not really the same. They may have different name.
		// This is to allow "update" the service discovery with some changed
		// info.... So in particular the "name" may contain some additional
		// information which can change at runtime
		for (ServiceEntity item : items) {
			if (this.items.contains(item)) {
				this.items.remove(item);
			}
			this.items.add(item);
		}
	}

	/**
	 * 2 ServiceEntities are equal of JIDs are equal and NODEs are equal.
	 *
	 * @param obj an <code>Object</code> value
	 *
	 * @return a <code>boolean</code> value
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServiceEntity) {
			ServiceEntity se = (ServiceEntity) obj;

			// Assuming here that jid can never be NULL
			// Node can be NULL so we need to do more calculation on it
			return ((se == null) ? false : jid.equals(se.jid)) &&
					((node == se.node) || ((node != null) ? node.equals(se.node) : se.node.equals(node)));
		}

		return false;
	}

	public ServiceEntity findNode(String node) {

		// System.out.println("Looking for a node: " + node);
		if (log.isLoggable(Level.FINEST)) {
			log.finest("Looking for a node: " + node);
		}
		if ((this.node != null) && this.node.equals(node)) {

			// System.out.println("Looking for a node: " + node);
			if (log.isLoggable(Level.FINEST)) {
				log.finest("Found myself: " + toString());
			}

			return this;
		}
		if (items == null) {
			return null;
		}
		for (ServiceEntity item : items) {
			String n = item.getNode();

			if ((n != null) && node.equals(n)) {
				if (log.isLoggable(Level.FINEST)) {
					log.finest("Found child item: " + item.toString());
				}

				return item;
			}
		}

		int idx = node.indexOf('/');

		if (idx >= 0) {
			String top = node.substring(0, idx);
			ServiceEntity current = findNode(top);

			if (current != null) {
				String rest = node.substring(idx + 1);

				return current.findNode(rest);
			}
		}

		return null;
	}

	public int hashCode() {
		return ((jid != null) ? jid.hashCode() : 0) + ((node != null) ? node.hashCode() : 0);
	}

	public void removeItems(ServiceEntity... items) {
		if (this.items == null) {
			return;
		}
		for (ServiceEntity item : items) {
			this.items.remove(item);
		}
	}

	@Override
	public String toString() {
		return getDiscoItem(null, null).toString();
	}

	public Optional<Element> getCaps(boolean admin, String domain) {
		if (adminOnly && !admin) {
			return Optional.empty();
		}
		final String[] discoFeatures = getDiscoFeatures();
		List<String> list = new ArrayList<>();
		for (ServiceIdentity serviceIdentity : getDiscoIdentities()) {
			String asCapsString = serviceIdentity.getAsCapsString();
			list.add(asCapsString);
		}
		list.sort(null);
		final String[] discoIdentities = list.toArray(new String[0]);

		Element extensions = extensionSupplier != null ? extensionSupplier.apply(domain) : null;
		final String caps = generateVerificationString(discoIdentities, discoFeatures, extensions);

		if (caps != null) {
			final String capsNode = CAPS_NODE + "#" + caps;
			PresenceCapabilitiesManager.setNodeFeatures(capsNode, getDiscoFeatures());
			return Optional.of(PresenceCapabilitiesManager.getCapsElement(caps));
		} else {
			return Optional.empty();
		}
	}

	public String[] getDiscoFeatures() {
		return features == null ? new String[0] : features.toArray(new String[0]);
	}

	public List<ServiceIdentity> getDiscoIdentities() {
		return identities == null ? Collections.emptyList() : Collections.unmodifiableList(identities);
	}

	public Element[] getDiscoFeatures(String node) {
		ArrayList<Element> elFeatures = new ArrayList<Element>();

		if (features != null) {
			for (String feature : features) {
				elFeatures.add(new Element("feature", new String[]{"var"}, new String[]{feature}));
			}
		}

		return (elFeatures.size() > 0) ? elFeatures.toArray(new Element[elFeatures.size()]) : null;
	}

	public Element getDiscoInfo(String node) {
		return getDiscoInfo(node, true);
	}

	public Element getDiscoInfo(String node, boolean admin) {

		// System.out.println("Node: " + node);
		if (log.isLoggable(Level.FINEST)) {
			log.finest("Node: " + node);
		}

		Element query = null;

		if (node == null) {

			// If the node is for admins only and this is not admin return null
			if (adminOnly && !admin) {
				return null;
			}
			if (log.isLoggable(Level.FINEST)) {
				log.finest("It's me: " + toString());
			}
			query = new Element("query", new String[]{"xmlns"}, new String[]{"http://jabber.org/protocol/disco#info"});
			if (identities != null) {
				for (ServiceIdentity ident : identities) {
					query.addChild(ident.getElement());
				}
			}
			if (features != null) {
				for (String feature : features) {
					query.addChild(new Element("feature", new String[]{"var"}, new String[]{feature}));
				}
			}
		} else {
			ServiceEntity entity = findNode(node);

			// If the entity is for admins only and this is not admin return null
			if ((entity != null) && entity.adminOnly && !admin) {
				entity = null;
			}
			if (entity != null) {
				if (log.isLoggable(Level.FINEST)) {
					log.finest("Found child node: " + entity.toString());
				}
				query = entity.getDiscoInfo(null);
				query.setAttribute("node", node);
			}
		}

		return query;
	}

	public Element getDiscoItem(String node, String jid) {
		Element item = new Element("item");

		if (jid != null) {
			item.setAttribute("jid", jid);
		} else {
			if (this.jid != null) {
				item.setAttribute("jid", this.jid);
			}
		}
		if (node != null) {
			item.setAttribute("node", node + ((this.node != null) ? "/" + this.node : ""));
		} else {
			if (this.node != null) {
				item.setAttribute("node", this.node);
			}
		}
		if (name != null) {
			item.setAttribute("name", name);
		}

		return item;
	}

	public List<Element> getDiscoItems(String node, String jid) {
		return getDiscoItems(node, jid, true);
	}

	public List<Element> getDiscoItems(String node, String jid, boolean admin) {

		// System.out.println("node: " + node + ", jid: " + jid);
		if (log.isLoggable(Level.FINEST)) {
			log.finest("node: " + node + ", jid: " + jid);
		}

		List<Element> result = null;

		if (node == null) {
			result = getItems(null, jid, admin);
		} else {
			ServiceEntity entity = findNode(node);

			if ((entity != null) && entity.adminOnly && !admin) {
				entity = null;
			}

			// System.out.println("Found disco entity: " + entity.toString());
			if (log.isLoggable(Level.FINEST)) {
				log.finest("Found disco entity: " + ((entity != null) ? entity.toString() : null));
			}
			if (entity != null) {
				result = entity.getItems(node, jid, admin);
			}
		}

		return result;
	}

	public List<Element> getItems(String node, String jid) {
		return getItems(node, jid, true);
	}

	public List<Element> getItems(String node, String jid, boolean admin) {
		List<Element> result = null;

		if (items != null) {
			result = new ArrayList<Element>();
			for (ServiceEntity item : items) {
				if (item.adminOnly && !admin) {
					continue;
				}
				result.add(item.getDiscoItem(node, jid));
			}
		}

		return result;
	}

	public String getJID() {
		return jid;
	}

	public String getName() {
		return name;
	}

	public String getNode() {
		return node;
	}

	public boolean isAdminOnly() {
		return adminOnly;
	}

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public void setDescription(String description) {
		this.name = description;
	}

	public void setFeatures(String... features) {
		this.features = null;
		addFeatures(features);
	}

	public void setIdentities(ServiceIdentity... identities) {
		this.identities = new ArrayList<ServiceIdentity>();
		Collections.addAll(this.identities, identities);
	}
}

