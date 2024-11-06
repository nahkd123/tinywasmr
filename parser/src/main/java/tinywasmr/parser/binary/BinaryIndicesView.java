package tinywasmr.parser.binary;

import java.util.List;

import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.type.FunctionType;

record BinaryIndicesView(List<FunctionType> types, List<FunctionDecl> functions) {
}