/**
 * 
 */
package org.minnal.autopojo.resolver;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.minnal.autopojo.util.PropertyUtil;

/**
 * @author ganeshs
 *
 */
public class MapResolver extends AbstractAttributeResolver {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<?, ?> resolve(Class<?> clazz, int maxDepth, Type... genericTypes) {
		Type genericKeyType = genericTypes != null && genericTypes.length > 0 ? genericTypes[0] : Object.class;
		Type genericValueType = genericTypes != null && genericTypes.length > 1 ? genericTypes[1] : Object.class;
		Class<?> keyType = PropertyUtil.getRawType(genericKeyType);
		Class<?> valueType = PropertyUtil.getRawType(genericValueType);
		Type[] keyParameters = PropertyUtil.getTypeArguments(genericKeyType);
		Type[] valueParameters = PropertyUtil.getTypeArguments(genericValueType);
		
		Map map = new HashMap();
		for (int i = 0; i < configuration.getNoOfElementsInCollection(); i++) {
			map.put(strategy.resolve(keyType, maxDepth, keyParameters), strategy.resolve(valueType, maxDepth, valueParameters));
		}
		return map;
	}

}
