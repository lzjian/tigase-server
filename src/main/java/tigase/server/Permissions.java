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
package tigase.server;

/**
 * Describe class Permissions here.
 * <br>
 * Created: Tue Jan 23 22:52:45 2007
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/
public enum Permissions {

	NONE,
	// Unknown user JID
	ANONYM,
	// Anonymous user
	REMOTE,
	// Packet from a user from a different XMPP installation
	LOCAL,
	// This is local user JID but not authenticated yet
	AUTH,
	// Local authenticated and authorized user
	TRUSTED,
	// Trusted account, can broadcast packets
	ADMIN;    // Admin account already authenticated

}
