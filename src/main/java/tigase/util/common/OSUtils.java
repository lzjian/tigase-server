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
package tigase.util.common;

/**
 * Created: Dec 11, 2008 10:26:00 AM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/
public class OSUtils {

	public enum OS {
		windows,
		linux,
		sunos,
		solaris,
		mac,
		unix,
		other;
	}

	public static OS getOSType() {
		String osName = System.getProperty("os.name").toLowerCase();
		for (OS os : OS.values()) {
			if (osName.contains(os.toString())) {
				return os;
			}
		}
		return OS.other;
	}

}
