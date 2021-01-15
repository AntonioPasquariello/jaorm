package io.jaorm.processor.strategy;

import com.squareup.javapoet.CodeBlock;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import java.util.List;

public interface ParametersStrategy {

    boolean isValid(String query);
    int getParamNumber(String query);
    List<CodeBlock> extract(ProcessingEnvironment procEnv, String query, ExecutableElement method);
    String replaceQuery(String query);
}
