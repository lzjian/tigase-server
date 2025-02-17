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
package tigase.conf;

import tigase.db.comp.RepositoryItemAbstract;
import tigase.server.Command;
import tigase.server.Packet;
import tigase.util.repository.DataTypes;
import tigase.xml.Element;

import java.util.logging.Logger;

/**
 * Created: Dec 10, 2009 2:40:26 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
*/
public class ConfigItem
		extends RepositoryItemAbstract {

	public static final String CLUSTER_NODE_ATTR = "cluster-node";

	public static final String COMPONENT_NAME_ATTR = "comp-name";

	// public static final String CLUSTER_NODE_LABEL = "Cluster node";

	public static final String COMPONENT_NAME_LABEL = "Component name";

	public static final String FLAG_ATTR = "flag";

	public static final String KEY_NAME_ATTR = "key-name";

	public static final String KEY_NAME_LABEL = "Property key name";

	public static final String NODE_NAME_ATTR = "node-name";

	public static final String NODE_NAME_LABEL = "Property node name";

	public static final String REPO_ITEM_ELEM_NAME = "prop";

	public static final String VALUE_ATTR = "value";

	public static final String VALUE_LABEL = "Propety value";

	public static final String VALUE_TYPE_ATTR = "value-type";

	private static final Logger log = Logger.getLogger(ConfigItem.class.getName());

	public enum FLAGS {
		INITIAL,
		DEFAULT,
		UPDATED;
	}
	private String clusterNode = null;
	private String compName = null;
	private FLAGS flag = FLAGS.DEFAULT;
	private String keyName = null;
	private long lastModificationTime = -1;
	private String nodeName = null;

	private Object value = null;

	@Override
	public void addCommandFields(Packet packet) {

		// Command.addFieldValue(packet, CLUSTER_NODE_LABEL,
		// (clusterNode != null ? clusterNode : ""));
		if (compName != null) {
			Command.addTextField(packet, COMPONENT_NAME_LABEL, compName);
		}
		if (nodeName != null) {
			Command.addTextField(packet, NODE_NAME_LABEL, nodeName);
		}
		Command.addTextField(packet, KEY_NAME_LABEL, ((keyName != null) ? keyName : ""));
		Command.addTextField(packet, "    ", "    ");

		String value_label = VALUE_LABEL;
		String value_str = "";

		if (value != null) {
			value_str = DataTypes.valueToString(value);
			value_label += " [" + DataTypes.getTypeId(value) + "]";
		}
		Command.addFieldValue(packet, value_label, value_str);
		super.addCommandFields(packet);
	}

	public String getClusterNode() {
		return clusterNode;
	}

	public String getCompName() {
		return compName;
	}

	/**
	 * Returns a configuration property key which is constructed in a following way: <code> nodeName + "/" + keyName
	 * </code>
	 */
	public String getConfigKey() {
		return ((nodeName != null) ? nodeName + "/" : "") + keyName;
	}

	/**
	 * Returns a configuration property value.
	 */
	public Object getConfigVal() {
		return value;
	}

	public String getConfigValToString() {
		return (value == null) ? null : DataTypes.valueToString(value);
	}

	@Override
	public String getElemName() {
		return REPO_ITEM_ELEM_NAME;
	}

	public FLAGS getFlag() {
		return flag;
	}

	/**
	 * {@inheritDoc}
	 * <br>
	 * Returns ConfigItem key which is constructed in a following way: {@code compName + "/" + nodeName + "/" +
	 * keyName}
	 */
	@Override
	public String getKey() {
		return ((compName != null) ? compName + "/" : "") + ((nodeName != null) ? nodeName + "/" : "") + keyName;
	}

	protected void setKey(String key) {
		// nothing to do..
	}

	/**
	 * Returns a property key which is constructed in a following way: {@code keyName}
	 */
	public String getKeyName() {
		return keyName;
	}

	public String getNodeName() {
		return nodeName;
	}

	@Override
	public void initFromCommand(Packet packet) {
		super.initFromCommand(packet);

		String tmp = Command.getFieldValue(packet, COMPONENT_NAME_LABEL);

		if ((tmp != null) && !tmp.isEmpty()) {
			compName = tmp;
		}
		tmp = Command.getFieldValue(packet, NODE_NAME_LABEL);
		if ((tmp != null) && !tmp.isEmpty()) {
			nodeName = tmp;
		}
		tmp = Command.getFieldValue(packet, KEY_NAME_LABEL);
		if ((tmp != null) && !tmp.isEmpty()) {
			keyName = tmp;
		}

		String value_label = Command.getFieldKeyStartingWith(packet, VALUE_LABEL);
		char t = DataTypes.decodeTypeIdFromName(value_label);

		tmp = Command.getFieldValue(packet, value_label);
		if ((tmp != null) && !tmp.isEmpty()) {
			value = DataTypes.decodeValueType(t, tmp);
		}
	}

	@Override
	public void initFromElement(Element elem) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void initFromPropertyString(String propString) {
		int idx_eq = propString.indexOf('=');

		// String key = prop[0].trim();
		// Object val = prop[1];
		String key = propString.substring(0, idx_eq);
		Object val = propString.substring(idx_eq + 1);
		String[] prop = idx_eq == -1 ? new String[]{propString} : new String[]{key, (String) val};

		if (key.matches(".*\\[[FLISBlisb]\\]$")) {
			char c = key.charAt(key.length() - 2);

			key = key.substring(0, key.length() - 3);
			val = DataTypes.decodeValueType(c, prop[1]);
		}

		int idx1 = key.indexOf("/");

		if (idx1 > 0) {
			String compNameMeth = key.substring(0, idx1);
			int idx2 = key.lastIndexOf("/");
			String nodeNameMeth = null;
			String keyNameMeth = key.substring(idx2 + 1);

			if (idx1 != idx2) {
				nodeNameMeth = key.substring(idx1 + 1, idx2);
			}
			set(compNameMeth, nodeNameMeth, keyNameMeth, val);
		} else {
			throw new IllegalArgumentException(
					"You have to provide a key with at least" + " 'component_name/key_name': " + key);
		}
	}

	public boolean isCompNodeKey(String comp, String node, String key) {
		return isComponent(comp) && isNode(node) && isKey(key);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ConfigItem) {
			return getKey().equals(((ConfigItem) o).getKey());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	/**
	 * Checks if the given component name is equal to this item compName.
	 *
	 */
	public boolean isComponent(String comp) {
		if (compName != comp) {
			return (compName != null) ? compName.equals(comp) : false;
		}

		return true;
	}

	/**
	 * Checks if the given key is equal to this item keyName.
	 *
	 */
	public boolean isKey(String key) {
		if (keyName != key) {
			return (keyName != null) ? keyName.equals(key) : false;
		}

		return true;
	}

	/**
	 * Checks if the given node is equal to this item nodeName
	 *
	 */
	public boolean isNode(String node) {
		if (nodeName != node) {

			// At least one is not null
			return (nodeName != null) ? nodeName.equals(node) : false;
		}

		return true;
	}

	/**
	 * Checks if the given node and key are equal to this item nodeName and keyName. This method call works the same way
	 * as following statement: {@code isNode(node) && isKey(key) }
	 */
	public boolean isNodeKey(String node, String key) {
		return isNode(node) && isKey(key);
	}

	public void set(String clusterNode_m, String compName_m, String nodeName_m, String key_m, String value_str_m,
					char val_type_m, String flag_str_m) {
		Object value_m = DataTypes.decodeValueType(val_type_m, value_str_m);
		FLAGS flag_m = FLAGS.DEFAULT;

		try {
			flag_m = FLAGS.valueOf(flag_str_m);
		} catch (Exception e) {
			log.warning("Incorrect config item flag: " + flag_str_m + ", setting DEFAULT.");
			flag_m = FLAGS.DEFAULT;
		}
		set(clusterNode_m, compName_m, nodeName_m, key_m, value_m, flag_m);
	}

	public void set(String clusterNode_m, String compName_m, String nodeName_m, String key_m, Object value_m,
					FLAGS flag_m) {
		if (clusterNode_m != null) {
			this.clusterNode = clusterNode_m;
		}
		if (compName_m != null) {
			this.compName = compName_m;
		}
		if (nodeName_m != null) {
			this.nodeName = nodeName_m;
		}
		if (key_m != null) {
			this.keyName = key_m;
		}
		if (value_m != null) {
			this.value = value_m;
		}
		if (flag_m != null) {
			this.flag = flag_m;
		}
		lastModificationTime = System.currentTimeMillis();
	}

	public void set(String compName_m, String nodeName_m, String key_m, String value_str_m, char val_type_m,
					String flag_str_m) {
		set(null, compName_m, nodeName_m, key_m, value_str_m, val_type_m, flag_str_m);
	}

	public void set(String compName, String nodeName, String key, Object value) {
		set(null, compName, nodeName, key, value, null);
	}

	public void set(String clusterNode, String compName, String nodeName, String key, Object value) {
		set(clusterNode, compName, nodeName, key, value, null);
	}

	public void setNodeKey(String clusterNode, String compName, String nodeKey, Object value) {
		int key_idx = nodeKey.lastIndexOf("/");
		String method_key = nodeKey;
		String method_node = null;

		if (key_idx >= 0) {
			method_key = nodeKey.substring(key_idx + 1);
			method_node = nodeKey.substring(0, key_idx);
		}
		set(clusterNode, compName, method_node, method_key, value);
	}

	// public static final String REPO_ITEM_ELEM_NAME = "prop";
	// public static final String CLUSTER_NODE_ATTR = "cluster-node";
	// public static final String COMPONENT_NAME_ATTR = "comp-name";
	// public static final String NODE_NAME_ATTR = "node-name";
	// public static final String KEY_NAME_ATTR = "key-name";
	// public static final String VALUE_ATTR = "value";
	// public static final String FLAG_ATTR = "flag";
	// public static final String VALUE_TYPE_ATTR = "value-type";
	//
	// private String clusterNode = null;
	// private String compName = null;
	// private String nodeName = null;
	// private String keyName = null;
	// private Object value = null;
	// private FLAGS flag = FLAGS.DEFAULT;

	@Override
	public Element toElement() {
		Element elem = super.toElement();

		if (clusterNode != null) {
			elem.addAttribute(CLUSTER_NODE_ATTR, clusterNode);
		}
		elem.addAttribute(COMPONENT_NAME_ATTR, compName);
		if (nodeName != null) {
			elem.addAttribute(NODE_NAME_ATTR, nodeName);
		}
		elem.addAttribute(KEY_NAME_ATTR, keyName);
		elem.addAttribute(VALUE_ATTR, DataTypes.valueToString(value));
		elem.addAttribute(VALUE_TYPE_ATTR, "" + DataTypes.getTypeId(value));
		elem.addAttribute(FLAG_ATTR, flag.name());

		return elem;
	}

	@Override
	public String toPropertyString() {
		char t = DataTypes.getTypeId(value);
		String result = getKey() + "[" + t + "]=";
		String varr = DataTypes.valueToString(value);

		result += varr;

		return result;
	}

	@Override
	public String toString() {
		return getKey() + "=" + value;
	}
}

