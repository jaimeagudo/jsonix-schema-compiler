package org.hisrc.jsonix.compilation.jsc;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;

import org.apache.commons.lang3.Validate;
import org.hisrc.jsonix.definition.Module;
import org.hisrc.jsonix.definition.Modules;
import org.hisrc.jsonix.definition.Output;
import org.hisrc.jsonix.jsonschema.JsonSchemaBuilder;

public class JsonSchemaModulesCompiler<T, C extends T> {

	private final Modules<T, C> modules;

	public JsonSchemaModulesCompiler(Modules<T, C> modules) {
		Validate.notNull(modules);
		this.modules = modules;
	}

	public void compile(JsonSchemaWriter<T, C> writer) {
		final JsonProvider provider = JsonProvider.provider();
		final JsonBuilderFactory builderFactory = provider
				.createBuilderFactory(null);

		for (final Module<T, C> module : this.modules.getModules()) {
			if (!module.isEmpty()) {
				for (Output output : module.getOutputs()) {
					final JsonSchemaModuleCompiler<T, C> moduleCompiler = new JsonSchemaModuleCompiler<T, C>(
							builderFactory, modules, module);
					final JsonSchemaBuilder schemaBuilder = moduleCompiler
							.compile();
					final JsonObject schema = schemaBuilder
							.build(builderFactory);
					writer.writeJsonSchema(module, schema, output);
				}
			}
		}
	}
}