package org.hisrc.jsonix.analysis;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MAnyAttributePropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MAnyElementPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MAttributePropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementRefPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementRefsPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementsPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MModelInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.MTyped;
import org.jvnet.jaxb2_commons.xml.bind.model.MValuePropertyInfo;

public final class PropertyInfoGraphBuilder<T, C extends T> implements
		MPropertyInfoVisitor<T, C, PropertyInfoVertex<T, C>> {

	private final ModelInfoGraphBuilder<T, C> modelInfoGraphBuilder;
	private final MModelInfo<T, C> modelInfo;
	private final PropertyInfoVertex<T, C> vertex;

	public PropertyInfoGraphBuilder(
			ModelInfoGraphBuilder<T, C> modelInfoGraphBuilder,
			MModelInfo<T, C> modelInfo, PropertyInfoVertex<T, C> vertex) {
		Validate.notNull(modelInfoGraphBuilder);
		Validate.notNull(modelInfo);
		Validate.notNull(vertex);
		this.modelInfoGraphBuilder = modelInfoGraphBuilder;
		this.modelInfo = modelInfo;
		this.vertex = vertex;
	}

	private void addHardDependencyOnType(
			PropertyInfoVertex<T, C> propertyInfoVertex, MTyped<T, C> info) {
		final InfoVertex<T, C> typeInfoVertex = modelInfoGraphBuilder.typeInfo(
				propertyInfoVertex.getPackageInfo(), info.getTypeInfo());
		modelInfoGraphBuilder.addHardDependency(propertyInfoVertex,
				typeInfoVertex);
	}

	private void addSoftDependencyOnType(
			PropertyInfoVertex<T, C> propertyInfoVertex, MTyped<T, C> info) {
		final InfoVertex<T, C> typeInfoVertex = modelInfoGraphBuilder.typeInfo(
				propertyInfoVertex.getPackageInfo(), info.getTypeInfo());
		modelInfoGraphBuilder.addSoftDependency(propertyInfoVertex,
				typeInfoVertex);
	}

	@Override
	public PropertyInfoVertex<T, C> visitValuePropertyInfo(
			MValuePropertyInfo<T, C> info) {
		// Value property depends on its type
		addHardDependencyOnType(vertex, info);
		return vertex;
	}

	@Override
	public PropertyInfoVertex<T, C> visitAttributePropertyInfo(
			MAttributePropertyInfo<T, C> info) {
		// Attribute property depends on its type
		addHardDependencyOnType(vertex, info);
		return vertex;
	}

	@Override
	public PropertyInfoVertex<T, C> visitAnyAttributePropertyInfo(
			MAnyAttributePropertyInfo<T, C> info) {
		return vertex;
	}

	@Override
	public PropertyInfoVertex<T, C> visitAnyElementPropertyInfo(
			MAnyElementPropertyInfo<T, C> info) {
		return vertex;
	}

	@Override
	public PropertyInfoVertex<T, C> visitElementPropertyInfo(
			MElementPropertyInfo<T, C> info) {
		// Element property depends on its type
		addHardDependencyOnType(vertex, info);
		return vertex;
	}

	@Override
	public PropertyInfoVertex<T, C> visitElementsPropertyInfo(
			MElementsPropertyInfo<T, C> info) {
		// Elements property has a "soft" dependency types of its elements
		// That is, some of these types may be excluded
		for (MElementTypeInfo<T, C> elementTypeInfo : info
				.getElementTypeInfos()) {
			addSoftDependencyOnType(vertex, elementTypeInfo);
		}
		return vertex;
	}

	@Override
	public PropertyInfoVertex<T, C> visitElementRefPropertyInfo(
			MElementRefPropertyInfo<T, C> info) {
		// Element reference property depends on the type of its element
		addHardDependencyOnType(vertex, info);
		addSubstitutionHeadDependencies(info, vertex);
		return vertex;
	}

	@Override
	public PropertyInfoVertex<T, C> visitElementRefsPropertyInfo(
			MElementRefsPropertyInfo<T, C> info) {
		for (MElementTypeInfo<T, C> elementTypeInfo : info
				.getElementTypeInfos()) {
			// Element references property has "soft" dependencies on the
			// types
			// of its elements
			addSoftDependencyOnType(vertex, elementTypeInfo);
			addSubstitutionHeadDependencies(elementTypeInfo, vertex);
		}
		return vertex;
	}

	private void addSubstitutionHeadDependencies(
			MElementTypeInfo<T, C> elementTypeInfo,
			PropertyInfoVertex<T, C> propertyInfoVertex) {
		final MClassInfo<T, C> classInfo = propertyInfoVertex.getClassInfo();
		final QName elementName = elementTypeInfo.getElementName();
		for (MElementInfo<T, C> elementInfo : this.modelInfo.getElementInfos()) {
			if (elementName.equals(elementInfo.getSubstitutionHead())
					&& (elementInfo.getScope() == null || elementInfo
							.getScope() == classInfo)) {
				final InfoVertex<T, C> elementInfoVertex = modelInfoGraphBuilder
						.elementInfo(elementInfo);
				modelInfoGraphBuilder.addSoftDependency(elementInfoVertex,
						propertyInfoVertex);
				modelInfoGraphBuilder.addSoftDependency(propertyInfoVertex, elementInfoVertex);
			}
		}
	}

}