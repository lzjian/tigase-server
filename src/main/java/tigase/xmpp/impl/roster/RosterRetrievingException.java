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
package tigase.xmpp.impl.roster;

import tigase.xmpp.XMPPException;

/**
 * @author kobit
 */
public class RosterRetrievingException
		extends XMPPException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new <code>PacketErrorTypeException</code> instance.
	 *
	 */
	public RosterRetrievingException(String message) {
		super(message);
	}

	public RosterRetrievingException(String message, Throwable cause) {
		super(message, cause);
	}
}

