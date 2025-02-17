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
package tigase.server.xmppserver.proc;

import tigase.kernel.beans.Bean;
import tigase.server.Packet;
import tigase.server.xmppserver.S2SConnectionManager;
import tigase.server.xmppserver.S2SIOService;

import java.util.Queue;
import java.util.logging.Logger;

/**
 * Created: Dec 9, 2010 2:00:08 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/
@Bean(name = "streamError", parent = S2SConnectionManager.class, active = true)
public class StreamError
		extends S2SAbstractProcessor {

	private static final Logger log = Logger.getLogger(StreamError.class.getName());

	@Override
	public int order() {
		return Order.StreamError.ordinal();
	}

	@Override
	public boolean process(Packet p, S2SIOService serv, Queue<Packet> results) {
		if (p.getElemName() == "error") {
			serv.stop();

			return true;
		} else {
			return false;
		}
	}
}
