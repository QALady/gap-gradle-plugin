package com.gap.gradle.utils

import groovy.json.JsonLexer
import groovy.json.JsonOutput
import groovy.json.JsonToken
import groovy.json.StringEscapeUtils

import static groovy.json.JsonTokenType.CLOSE_BRACKET
import static groovy.json.JsonTokenType.CLOSE_CURLY
import static groovy.json.JsonTokenType.COLON
import static groovy.json.JsonTokenType.COMMA
import static groovy.json.JsonTokenType.OPEN_BRACKET
import static groovy.json.JsonTokenType.OPEN_CURLY
import static groovy.json.JsonTokenType.STRING

class GradleOutput extends JsonOutput{

	static String prettyPrint(String jsonPayload) {
		int indent = 0
		def output = new StringBuilder()
		def lexer = new JsonLexer(new StringReader(jsonPayload))

		while (lexer.hasNext()) {
			JsonToken token = lexer.next()
			if (token.type == OPEN_CURLY) {
				indent += 4
				output.append('{\n')
				output.append(' ' * indent)
			} else if (token.type == CLOSE_CURLY) {
				indent -= 4
				output.append('\n')
				output.append(' ' * indent)
				output.append('}')
			} else if(token.type == OPEN_BRACKET) {
				indent += 4
				output.append('[\n')
				output.append(' ' * indent)
			} else if(token.type == CLOSE_BRACKET) {
				indent -= 4
				output.append('\n')
				output.append(' ' * indent)
				output.append(']')
			} else if (token.type == COMMA) {
				output.append('\n')
				output.append(' ' * indent)
			} else if (token.type == COLON) {
				output.append(' ')
			} else if (token.type == STRING) {
				// Cannot use a range (1..-2) here as it will reverse for a string of
				// length 2 (i.e. textStr=/""/ ) and will not strip the leading/trailing
				// quotes (just reverses them).
				String textStr = token.text
				String textWithoutQuotes = textStr.substring( 1, textStr.size()-1 )
				output.append("'" + StringEscapeUtils.escapeJava( textWithoutQuotes ) + "'")
			} else {
				output.append(token.text)
			}
		}

		return output.toString()
	}
}
