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
package tigase.server.amp.cond;

import tigase.server.Packet;
import tigase.server.amp.ConditionIfc;
import tigase.xml.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Created: Apr 27, 2010 5:36:39 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/
public class ExpireAt
		implements ConditionIfc {

	public static final String NAME = "expire-at";
	private static Logger log = Logger.getLogger(ExpireAt.class.getName());

	private final SimpleDateFormat formatter;
	private final SimpleDateFormat formatter2;

	{
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter2.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean match(Packet packet, Element rule) {
		String value = rule.getAttributeStaticStr("value");

		if (value != null) {
			try {
				Date val_date = null;

				if (value.contains(".")) {
					synchronized (formatter) {
						val_date = formatter.parse(value);
					}
				} else {
					synchronized (formatter2) {
						val_date = formatter2.parse(value);
					}
				}

				return val_date.before(new Date());
			} catch (ParseException ex) {
				log.info("Incorrect " + NAME + " condition value for rule: " + rule);
			}
		} else {
			log.info("No value set for rule: " + rule);
		}

		return false;
	}
}

