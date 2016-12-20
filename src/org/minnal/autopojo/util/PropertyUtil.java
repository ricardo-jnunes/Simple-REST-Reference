/**
 * 
 */
package org.minnal.autopojo.util;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.common.reflect.TypeToken;

/**
 * @author ganeshs
 *
 */
public class PropertyUtil {
	
	/**
	 * Check if the given type represents a "simple" property:
	 * a primitive, a String or other CharSequence, a Number, a Date,
	 * a URI, a URL, a Locale, a Class, or a corresponding array.
	 * <p>Used to determine properties to check for a "simple" dependency-check.
	 * @param clazz the type to check
	 * @return whether the given type represents a "simple" property
	 * @see org.springframework.beans.factory.support.RootBeanDefinition#DEPENDENCY_CHECK_SIMPLE
	 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#checkDependencies
	 */
	public static boolean isSimpleProperty(Class<?> clazz) {
		return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
	}

	/**
	 * Check if the given type represents a "simple" value type:
	 * a primitive, a String or other CharSequence, a Number, a Date,
	 * a URI, a URL, a Locale or a Class.
	 * 
	 * @param clazz the type to check
	 * @return whether the given type represents a "simple" value type
	 */
	public static boolean isSimpleValueType(Class<?> clazz) {
		return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() ||
				CharSequence.class.isAssignableFrom(clazz) ||
				Number.class.isAssignableFrom(clazz) ||
				Date.class.isAssignableFrom(clazz) ||
				clazz.equals(URI.class) || clazz.equals(URL.class) ||
				clazz.equals(Locale.class) || clazz.equals(Class.class) || 
				clazz.equals(Serializable.class) || clazz.equals(Timestamp.class);
	}
	
	public static boolean isCollectionProperty(Type type, boolean includeMaps) {
		if (type instanceof Class) {
			return isCollectionProperty((Class<?>)type, includeMaps);
		}
		if (type instanceof ParameterizedType) {
			return isCollectionProperty((Class<?>)((ParameterizedType) type).getRawType(), includeMaps);
		}
		return false;
	}
	
	public static boolean isCollectionProperty(Class<?> clazz, boolean includeMaps) {
		return Collection.class.isAssignableFrom(clazz) || (includeMaps && isMapProperty(clazz));
	}
	
	public static boolean isMapProperty(Type type) {
		if (type instanceof Class) {
			return isMapProperty((Class<?>)type);
		}
		if (type instanceof ParameterizedType) {
			return isMapProperty((Class<?>)((ParameterizedType) type).getRawType());
		}
		return false;
	}
	
	public static boolean isMapProperty(Class<?> clazz) {
		return Map.class.isAssignableFrom(clazz);
	}
	
	public static Class<?> getRawType(Type genericType) {
		if (genericType instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) genericType;
			return (Class<?>) ptype.getRawType();
		}
		return (Class<?>) genericType;
	}
	
	public static Type[] getTypeArguments(Type genericType) {
		if (genericType instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) genericType;
			return ptype.getActualTypeArguments();
		}
		return null;
	}
	
	public static Class<?> getMapKeyType(Type type) {
		return TypeToken.of(type).resolveType(getMapGenericTypes(type)[0]).getRawType();
	}
	
	public static Class<?> getMapValueType(Type type) {
		return TypeToken.of(type).resolveType(getMapGenericTypes(type)[1]).getRawType();
	}
	
	private static Type[] getMapGenericTypes(Type type) {
		try {
			return Map.class.getMethod("put", Object.class, Object.class).getGenericParameterTypes();
		} catch (Exception e) {
			// TODO logo exception and ignore
		}
		return new Type[]{Object.class, Object.class};
	}
	
	public static Class<?> getCollectionElementType(Type type) {
		Class<?> rawType = getRawType(type);
		
		try {
			if (Collection.class.isAssignableFrom(rawType)) {
				return TypeToken.of(type).resolveType(Collection.class.getMethod("add", Object.class).getGenericParameterTypes()[0]).getRawType();
			}
			if (Map.class.isAssignableFrom(rawType)) {
				return getMapValueType(type);
			}
		} catch (Exception e) {
			// TODO logo exception and ignore
		}
		return Object.class;
	}
	
	public static List<Annotation> getAnnotations(PropertyDescriptor descriptor) {
		List<Annotation> annotations = new ArrayList<Annotation>();
		annotations.addAll(getMethodAnnotations(descriptor));
		annotations.addAll(getFieldAnnotations(descriptor));
		return annotations;
	}
	
	public static List<Annotation> getMethodAnnotations(PropertyDescriptor descriptor) {
		List<Annotation> annotations = new ArrayList<Annotation>();
		Method method = descriptor.getReadMethod();
		if (method == null || method.getDeclaringClass().equals(Object.class)) {
			return annotations;
		}
		annotations.addAll(Arrays.asList(method.getAnnotations()));
		return annotations;
	}
	
	public static List<Annotation> getFieldAnnotations(PropertyDescriptor descriptor) {
		List<Annotation> annotations = new ArrayList<Annotation>();
		Method method = descriptor.getReadMethod();
		if (method == null || method.getDeclaringClass().equals(Object.class)) {
			return annotations;
		}
		Field field = FieldUtils.getField(method.getDeclaringClass(), descriptor.getName(), true);
		if (field == null) {
			return annotations;
		}
		annotations.addAll(Arrays.asList(field.getAnnotations()));
		return annotations;
	}
	
	public static <T extends Annotation> T getAnnotation(PropertyDescriptor descriptor, Class<T> clazz) {
		return getAnnotation(descriptor, clazz, false);
	}
	
	public static <T extends Annotation> T getAnnotation(PropertyDescriptor descriptor, Class<T> clazz, boolean ignoreObjectClass) {
		Method method = descriptor.getReadMethod();
		if (method == null || ignoreObjectClass && method.getDeclaringClass().equals(Object.class)) {
			return null;
		}
		
		Field field = FieldUtils.getField(method.getDeclaringClass(), descriptor.getName(), true);
		if (field == null) {
			return null;
		}
		if (field.isAnnotationPresent(clazz)) {
			return field.getAnnotation(clazz);
		}
		return method.getAnnotation(clazz);
	}
	
	public static boolean hasAnnotation(PropertyDescriptor descriptor, Class<? extends Annotation> clazz) {
		return hasAnnotation(descriptor, clazz, false);
	}
	
	public static boolean hasAnnotation(PropertyDescriptor descriptor, Class<? extends Annotation> clazz, boolean ignoreObjectClass) {
		return getAnnotation(descriptor, clazz) != null;
	}
	
	public static List<String> getEnumValues(PropertyDescriptor descriptor) {
		List<String> values = new ArrayList<String>();
		try {
			Enum<?>[] enums = (Enum[])descriptor.getPropertyType().getMethod("values").invoke(null);
			for (Enum<?> enm : enums) {
				values.add(enm.name());
			}
		} catch (Exception e) {
			// TODO Log exception and ignore
		}
		return values;
	}
	
	public static Type getGenericType(PropertyDescriptor descriptor) {
		Method method = descriptor.getReadMethod();
		if (method == null) {
			method = descriptor.getWriteMethod();
		}
		if (method == null) {
			return null;
		}
		return method.getGenericReturnType();
	}
	
	public static Class<?> getType(PropertyDescriptor descriptor) {
		Type type = getGenericType(descriptor);
		if (type instanceof ParameterizedType) {
			if (isCollectionProperty(type, true)) {
				return getCollectionElementType(type);
			}
			return (Class<?>)((ParameterizedType)type).getRawType(); 
		}
		return (Class<?>) type;
	}

	public static List<PropertyDescriptor> getPopertiesWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
		List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
		for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(clazz)) {
			if (hasAnnotation(descriptor, annotation)) {
				descriptors.add(descriptor);
			}
		}
		return descriptors;
	}
	
	public static PropertyDescriptor getDescriptor(Class<?> clazz, String property) {
		for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(clazz)) {
			if (descriptor.getName().equals(property)) {
				return descriptor;
			}
		}
		return null;
	}
	
	public static boolean isPrimitive(Class<?> type) {
		return type.equals(int.class) || type.equals(long.class) || type.equals(char.class) || type.equals(byte.class) || type.equals(boolean.class) || 
				type.equals(short.class) || type.equals(double.class) || type.equals(float.class);
	}
	
	public static boolean isPrimitive(PropertyDescriptor descriptor) {
		return isPrimitive(descriptor.getPropertyType());
		
	}
}
