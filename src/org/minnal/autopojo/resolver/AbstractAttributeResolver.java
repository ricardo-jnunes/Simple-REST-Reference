/**
 * 
 */
package org.minnal.autopojo.resolver;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.minnal.autopojo.AttributeMetaData;
import org.minnal.autopojo.AutoPojoException;
import org.minnal.autopojo.Configuration;
import org.minnal.autopojo.GenerationStrategy;
import org.minnal.autopojo.util.PropertyUtil;

/**
 * @author ganeshs
 *
 */
public abstract class AbstractAttributeResolver implements AttributeResolver {
	
	protected GenerationStrategy strategy;
	
	protected Configuration configuration;
	
	protected void setAttribute(Object pojo, AttributeMetaData attribute, Object value) {
		try {
			if (value != null) {
				PropertyUtils.setProperty(pojo, attribute.getName(), value);
			} else if (PropertyUtil.isPrimitive(attribute.getType())) {
				PropertyUtils.setProperty(pojo, attribute.getName(), ConvertUtils.convert(0, attribute.getType()));
			}
		} catch (Exception e) {
			throw new AutoPojoException(e);
		}
	}
	
	public void resolve(Object pojo, AttributeMetaData attribute, int maxDepth) {
		Object value = maxDepth >= 0 ? resolve(attribute.getType(), maxDepth, attribute.getTypeArguments()) : null;
		setAttribute(pojo, attribute, value);
	}

	@Override
	public void init(GenerationStrategy strategy, Configuration configuration) {
		this.strategy = strategy;
		this.configuration = configuration;
	}
}
