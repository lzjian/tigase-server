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
package tigase.auth.mechanisms;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslException;
import java.util.Map;

public class SaslSCRAMSha512
		extends AbstractSaslSCRAM {

	public final static String NAME = "SCRAM-SHA-512";
	protected final static String ALGO = "SHA-512";

	public SaslSCRAMSha512(Map<? super String, ?> props, CallbackHandler callbackHandler) {
		super(NAME, ALGO, DEFAULT_CLIENT_KEY, DEFAULT_SERVER_KEY, props, callbackHandler);
	}

	SaslSCRAMSha512(Map<? super String, ?> props, CallbackHandler callbackHandler, String once) {
		super(NAME, ALGO, DEFAULT_CLIENT_KEY, DEFAULT_SERVER_KEY, props, callbackHandler, once);
	}

	@Override
	protected void checkRequestedBindType(BindType requestedBindType) throws SaslException {
		switch (requestedBindType) {
			case n:
				break;
			case y:
				// throw new SaslException("Server supports PLUS. Please use 'p'");
				break;
			case tls_server_end_point:
			case tls_unique:
				throw new SaslException("Invalid request for " + NAME);
		}
	}

}
