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
package tigase.util;

import org.junit.Test;
import tigase.TestLogger;
import tigase.util.repository.DataTypes;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Wojtek
 */
public class DataTypesTest {

	private static final Logger log = TestLogger.getLogger(DataTypesTest.class);

	@Test
	public void testParseNum() {

		assertEquals(new Long(262144L), Long.valueOf(Integer.class.cast(DataTypes.parseNum("256k", Integer.class, 1))));
		assertEquals(new Long(262144L), Long.class.cast(DataTypes.parseNum("256k", Long.class, 1L)));
		assertEquals(new Double(670720.0D), Double.class.cast(DataTypes.parseNum("655k", Double.class, 1D)));
		assertEquals(new Double(262144F),
					 Double.valueOf(Float.class.cast(DataTypes.parseNum("256k", Float.class, 1F))));
		assertEquals(new Long(25), Long.valueOf((long) DataTypes.parseNum("25", Short.class, Short.valueOf("1"))));
		assertEquals(new Long(25),
					 Long.valueOf(Byte.class.cast(DataTypes.parseNum("25", Byte.class, Byte.valueOf("1")))));
	}

	@Test
	public void testParseSizeInt() {
		log.log(Level.FINE, "parseSizeInt");
		assertEquals(1, DataTypes.parseSizeInt("1", 1));
		assertEquals(1024, DataTypes.parseSizeInt("1k", 1));
		assertEquals(1024 * 1024, DataTypes.parseSizeInt("1m", 1));
		assertEquals(1024 * 1024 * 1024, DataTypes.parseSizeInt("1g", 1));
		assertEquals(1, DataTypes.parseSizeInt("fail", 1));
	}

	@Test
	public void testNull() {
		final Object o = DataTypes.decodeValueType('S', "null");
		assertNull(o);
	}
}
