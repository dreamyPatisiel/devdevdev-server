package com.dreamypatisiel.devdevdev.global.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

import static org.hibernate.type.StandardBasicTypes.DOUBLE;

public class CustomMySQLFunctionContributor implements FunctionContributor {
    private static final String MATCH_AGAINST_FUNCTION = "match_against";
    private static final String MATCH_AGAINST_PATTERN = "match (?1) against (?2 in boolean mode)";
    
    private static final String MATCH_AGAINST_NL_FUNCTION = "match_against_nl";
    private static final String MATCH_AGAINST_NL_PATTERN = "match (?1) against (?2 in natural language mode)";

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry()
                .registerPattern(MATCH_AGAINST_FUNCTION, MATCH_AGAINST_PATTERN,
                        functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(DOUBLE));
        
        functionContributions.getFunctionRegistry()
                .registerPattern(MATCH_AGAINST_NL_FUNCTION, MATCH_AGAINST_NL_PATTERN,
                        functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(DOUBLE));
    }
}
