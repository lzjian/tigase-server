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
package tigase.server.xmppserver;

import tigase.server.Packet;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created: Jun 14, 2010 1:19:55 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/
public class S2SConnection
		implements Comparable<S2SConnection> {

	private static final Logger log = Logger.getLogger(S2SConnection.class.getName());

	private OutgoingState conn_state = OutgoingState.NULL;
	private S2SConnectionHandlerIfc<S2SIOService> handler = null;
	private String ipAddress = null;
	private S2SIOService service = null;
	/**
	 * Control packets for s2s connection establishing
	 */
	private ConcurrentLinkedQueue<Packet> waitingControlPackets = new ConcurrentLinkedQueue<Packet>();

	public S2SConnection(S2SConnectionHandlerIfc<S2SIOService> handler, String ip) {
		this.handler = handler;
		this.ipAddress = ip;
	}

	public void addControlPacket(Packet packet) {
		waitingControlPackets.add(packet);
	}

	@Override
	public int compareTo(S2SConnection o) {
		return hashCode() - o.hashCode();
	}

	public String getIPAddress() {
		return ipAddress;
	}

	public S2SIOService getS2SIOService() {
		return service;
	}

	public void setS2SIOService(S2SIOService serv) {
		this.service = serv;
	}

	public int getWaitingControlCount() {
		return waitingControlPackets.size();
	}

	public boolean isConnected() {
		return (service != null) && service.isConnected();
	}

	public void sendAllControlPackets() {
		if (log.isLoggable(Level.FINEST)) {
			for (Packet packet : waitingControlPackets) {
				log.log(Level.FINEST, "Sending on connection: {0} control packet: {1}", new Object[]{service, packet});
			}
		}

		handler.writePacketsToSocket(service, waitingControlPackets);
	}

	public boolean sendPacket(Packet packet) throws IOException {
		return handler.writePacketToSocket(service, packet);
	}

	@Override
	public String toString() {
		return "S2S: " + service;
	}
}
