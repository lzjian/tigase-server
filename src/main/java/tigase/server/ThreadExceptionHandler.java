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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class ThreadExceptionHandler.java is responsible helper class used to catch all unhandled exception from all threads.
 * This is default handler which sends exception stack trace to log system. If necessary other server packages can use
 * own custom handlers and do something else with unhandled exceptions. This handler is only implemented to avoid hidden
 * exception causing bugs.
 * <br>
 * <p> Created: Thu Sep 30 22:24:24 2004 </p>
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/

public class ThreadExceptionHandler
		implements Thread.UncaughtExceptionHandler {

	private static final Logger log = Logger.getLogger("tigase.server.ThreadExceptionHandler");

	public void uncaughtException(final Thread t, final Throwable e) {
		log.log(Level.SEVERE, "Uncaught thread: \"" + t.getName() + "\" exception", e);
	}

}// ThreadExceptionHandler
