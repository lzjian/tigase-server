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
package tigase.xmpp.impl;

import org.junit.After;
import org.junit.Before;
import tigase.component.exceptions.RepositoryException;
import tigase.db.DataSource;
import tigase.db.DataSourceAware;
import tigase.db.DataSourceHelper;
import tigase.kernel.core.Kernel;

/**
 * Class is a base class for testing processors which require instance of class implementing DataSourceAware interface.
 * 
 * */
public abstract class AbstractProcessorWithDataSourceAwareTestCase<DS extends DataSource, R extends DataSourceAware> extends AbstractProcessorWithDataSourceTestCase<DS> {

	protected R repo;

	protected abstract Class<? extends DataSourceAware> getDataSourceAwareIfc();

	@Before
	public void setupDataSourceAware() throws Exception {
		repo = prepareDataSourceAware();
	}

	@After
	public void tearDown() throws Exception {
		repo = null;
	}

	@Override
	protected void registerBeans(Kernel kernel) {
		super.registerBeans(kernel);
		try {
			Class dataSourceAwareClassForUri = DataSourceHelper.getDefaultClass(getDataSourceAwareIfc(), uri);
			kernel.registerBean("repository").asClass(dataSourceAwareClassForUri).setActive(true).exec();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected R prepareDataSourceAware() throws Exception {
		R repo = getInstance((Class<R>) getDataSourceAwareIfc());
		try {
			// we do not check schema version as we are updating schema in loadSchema() method!!
			//dataSource.checkSchemaVersion(repo, true);
			repo.setDataSource(dataSource);
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw new RepositoryException(ex);
		}
		return repo;
	}

}
