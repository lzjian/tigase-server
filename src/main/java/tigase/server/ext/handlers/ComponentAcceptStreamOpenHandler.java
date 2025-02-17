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
package tigase.server.ext.handlers;

import tigase.server.Packet;
import tigase.server.ext.*;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Logger;

import static tigase.server.ext.ComponentProtocolHandler.REPO_ITEM_KEY;

/**
 * Created: Oct 7, 2009 5:51:47 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/
public class ComponentAcceptStreamOpenHandler
		implements StreamOpenHandler {

	public static final String XMLNS = "jabber:component:accept";
	private static final Logger log = Logger.getLogger(ComponentAcceptStreamOpenHandler.class.getName());

	private String[] xmlnss = new String[]{XMLNS};

	@Override
	public String[] getXMLNSs() {
		return xmlnss;
	}

	@Override
	public String serviceStarted(ComponentIOService serv) {
		switch (serv.connectionType()) {
			case connect:
				CompRepoItem repoItem = (CompRepoItem) serv.getSessionData().get(REPO_ITEM_KEY);
				String r_host = (String) serv.getSessionData().get("remote-host");

				// Send init xmpp stream here
				serv.getSessionData().put(ComponentIOService.HOSTNAME_KEY, r_host);

				// This should be done only, after authentication is completed
				// addComponentConnection(hostname, serv);
				String data = "<stream:stream" + " xmlns='" + XMLNS + "'" +
						" xmlns:stream='http://etherx.jabber.org/streams'" + " to='" + repoItem.getDomain() + "'" + ">";

				return data;

			default:

				// Do nothing, more/some data should come soon...
				break;
		}    // end of switch (service.connectionType())

		return null;
	}

	@Override
	public String streamOpened(ComponentIOService serv, Map<String, String> attribs, ComponentProtocolHandler handler) {
		serv.getSessionData().put(XMLNS_KEY, XMLNS);

		switch (serv.connectionType()) {
			case connect: {
				String id = attribs.get("id");

				if (id == null) {
					serv.stop();

					return null;
				}

				serv.getSessionData().put(ComponentIOService.SESSION_ID_KEY, id);

				ExtProcessor proc = handler.getProcessor("handshake");

				if (proc != null) {
					Queue<Packet> results = new ArrayDeque<Packet>();

					proc.startProcessing(null, serv, handler, results);

					if (results != null) {
						StringBuilder sb = new StringBuilder();

						for (Packet p : results) {
							sb.append(p.getElement().toString());
						}

						return sb.toString();
					}
				} else {
					log.warning("Required processor is not available: 'handshake'");
				}

				return null;
			}

			case accept: {
				String hostname = attribs.get("to");
				CompRepoItem repoItem = handler.getCompRepoItem(hostname);

				if (repoItem != null) {
					serv.getSessionData().put(REPO_ITEM_KEY, repoItem);
					serv.getSessionData().put(ComponentIOService.HOSTNAME_KEY, hostname);
					log.finest("CompRepoItem for " + hostname + " set: " + repoItem.toString());

					String id = UUID.randomUUID().toString();

					serv.getSessionData().put(ComponentIOService.SESSION_ID_KEY, id);
					log.finest("ID generated and set: " + id);

					// This should be done only, after authentication is completed
					// addComponentConnection(hostname, serv);
					return "<stream:stream" + " xmlns='" + XMLNS + "'" +
							" xmlns:stream='http://etherx.jabber.org/streams'" + " from='" + hostname + "'" + " id='" +
							id + "'" + ">";
				} else {
					return "<stream:stream" + " xmlns='" + XMLNS + "'" +
							" xmlns:stream='http://etherx.jabber.org/streams'" + " from='" + hostname + "'>" +
							"<stream:error>" + "<host-unknown xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>" +
							"</stream:error></stream:stream>";
				}
			}

			default:

				// Do nothing, more data should come soon...
				break;
		}    // end of switch (service.connectionType())

		return null;
	}
}

