package utils;

public class Regex {
	
	public static final String REGEX_ADDRESSLESS_INSTRUCTIONS = "push|PUSH|pop|POP|not|NOT|(i){0,1}ret|(I){0,1}RET|halt|HALT|inc|INC|dec|DEC|cli|CLI|sti|STI|ldsp|LDSP|stsp|stSP|ldimr|LDIMR|stimr|STIMR";
	public static final String REGEX_ADDRESS_INSTRUCTIONS = "ld|LD|st|ST|add|ADD|sub|SUB|and|AND|or|OR|xor|XOR|mul|MUL|cmp|CMP";
	public static final String REGEX_JUMP_INSTRUCTIONS = "jmp|JMP|jgt|JGT|jge|JGE|jlt|JLT|jle|JLE|jeq|JEQ|jneq|JNEQ|call|CALL";

	public static final String REGEX_COMMENT_TEXT = ".+";
	public static final String REGEX_DIRECTIVES = "rept|endr|section|byte|word|skip|int|asciz|ascii";
	public static final String REGEX_SECTION_START = "\\.section";
	public static final String REGEX_DIRECTIVE = "\\.[a-zA-Z_][a-zA-Z0-9_]*";
	public static final String REGEX_NUMBER_HEXADECIMAL = "0x([0]+|[1-9a-fA-F][0-9a-fA-F]*)";
	public static final String REGEX_NUMBER_BINARY = "0b[01][01_]*";
	public static final String REGEX_NUMBER_DECIMAL = "[0]+|[1-9][0-9]*";
	public static final String REGEX_NUMBER = REGEX_NUMBER_DECIMAL + "|" + REGEX_NUMBER_HEXADECIMAL + "|" + REGEX_NUMBER_BINARY;
	public static final String REGEX_LABEL_NAME = "[a-zA-Z_][a-zA-Z0-9_]*";
	public static final String REGEX_ADDRESSING_IMMEDIATE = "#(" + REGEX_NUMBER + "|" + REGEX_LABEL_NAME + ")";
	public static final String REGEX_ADDRESSING_REGISTER_DIRECT = "[rR]([0-9]|1[0-9]|2[0-9]|3[0-1])";
	public static final String REGEX_ADDRESSING_REGISTER_DIRECT_PREINCR = "\\+\\+\\[" + REGEX_ADDRESSING_REGISTER_DIRECT + "\\]";
	public static final String REGEX_ADDRESSING_REGISTER_INDIRECT_DISP = "\\[" + REGEX_ADDRESSING_REGISTER_DIRECT + "\\]\\,(" + REGEX_NUMBER + ")";
	public static final String REGEX_ADDRESSING_REGISTER_DIRECT_POSTDECR = "\\[" + REGEX_ADDRESSING_REGISTER_DIRECT + "\\]\\-\\-";
	public static final String REGEX_ADDRESSING_REGISTER_INDIRECT = "\\[" + REGEX_ADDRESSING_REGISTER_DIRECT + "\\]";
	public static final String REGEX_ADDRESSING_MEMORY_DIRECT = REGEX_LABEL_NAME + "|" + REGEX_NUMBER_HEXADECIMAL;
	public static final String REGEX_ADDRESSING_MEMORY_INDIRECT = "\\[" + REGEX_ADDRESSING_MEMORY_DIRECT + "\\]";
	public static final String REGEX_LABEL_DECL =  REGEX_LABEL_NAME + "\\:";
	
	public static final String REGEX_INSTRUCTION = "(" + 
												   REGEX_ADDRESSLESS_INSTRUCTIONS 			+ "|" + 
												   REGEX_ADDRESS_INSTRUCTIONS 				+ "|" +
												   REGEX_JUMP_INSTRUCTIONS
												   + ")";
	
	public static final String REGEX_COMMENT_START = "@(.*)";

	public static final String REGEX_ADDRESSING = "(" +
			REGEX_ADDRESSING_REGISTER_DIRECT + "|" +
			REGEX_ADDRESSING_REGISTER_INDIRECT + "|" +
			REGEX_ADDRESSING_IMMEDIATE + "|" +
			REGEX_ADDRESSING_REGISTER_DIRECT_POSTDECR + "|" +
			REGEX_ADDRESSING_REGISTER_DIRECT_PREINCR + "|" +
			REGEX_ADDRESSING_REGISTER_INDIRECT_DISP + "|" +
			REGEX_ADDRESSING_MEMORY_DIRECT + "|" +
			REGEX_ADDRESSING_MEMORY_INDIRECT
			+ ")";

	public static final String REGEX_PROGRAM_AVAILABLE_REGISTERS = "(" +
			"pc|PC|psw|PSW|ax|AX|sp|SP|" + REGEX_ADDRESSING_REGISTER_DIRECT +
			")";

	public static final String REGEX_INSTRUCTION_LINE =
		REGEX_INSTRUCTION + "([ \\t]*)" + REGEX_ADDRESSING + "?";

	public static final int getRegisterNumber( String text ) {
		return Integer.parseInt( text.replaceAll("^[rR]", "") );
	}

	public static void main(String[] varg) {
		System.out.println( "ASD".matches(REGEX_PROGRAM_AVAILABLE_REGISTERS) );
	}
}
