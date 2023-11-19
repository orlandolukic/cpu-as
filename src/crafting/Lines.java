package crafting;

import checkings.LineChecker;
import controller.MainScreenController;
import crafting.addressing.Addressing;
import crafting.addressing.MemoryDirect;
import crafting.addressing.RegisterDirect;
import crafting.cells.*;
import debug.registers.Register;
import jdk.jshell.execution.Util;
import utils.Regex;
import utils.Styles;
import utils.Utilities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static utils.Regex.*;

public class Lines {

    public static LineChecker ADDRESSLESS_INSTRUCTIONS = new LineChecker(
        new LineCell[] {
                new InstructionCell( REGEX_ADDRESSLESS_INSTRUCTIONS )
        }
    );

    public static LineChecker ADDRESS_INSTRUCTIONS = new LineChecker(
            new LineCell[] {
                    new InstructionCell( REGEX_ADDRESS_INSTRUCTIONS ),
                    new AddressingCell( AddressingCell.ALLOWED_ALL_ADDRESSINGS )
            }
    );

    public static LineChecker JUMP_INSTRUCTIONS = new LineChecker(
            new LineCell[] {
                    new InstructionCell( REGEX_JUMP_INSTRUCTIONS ),
                    new LabelCell()
            }
    );

    /**
     * Directives
     */
    public static LineChecker SECTION_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.section" ),
                    new DirectiveCell(REGEX_DIRECTIVE)
            }
    );

    public static LineChecker REPT_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.rept" ),
                    new NumberCell( NumberCell.DECIMAL )
            }
    );

    public static LineChecker ENDR_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.endr" )
            }
    );

    public static LineChecker WORD_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.(word|int)" ),
                    new RepetitionCommaCell( new LineCell[] {
                            new CombinedCell( new LineCell[] {
                                    new NumberCell( NumberCell.ALL ),
                                    new LabelCell()
                            }, ",$|^,")
                    }, RepetitionCommaCell.INFINITE )
            }
    );

    public static LineChecker BYTE_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.byte" ),
                    new RepetitionCommaCell( new LineCell[] {
                            new CombinedCell( new LineCell[] {
                                    new NumberCell( NumberCell.ALL )
                            }, ",$|^,")
                    }, RepetitionCommaCell.INFINITE )
            }
    );

    public static LineChecker SKIP_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.skip" ),
                    new NumberCell( NumberCell.DECIMAL )
            }
    );

    public static LineChecker PLACE_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.place" ),
                    new NumberCell( NumberCell.HEXADECIMAL )
            }
    );

    public static LineChecker ASCIIZ_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.asci(i|z)" ),
                    new DirectiveCell( "\"(.*)" ),
                    new RepetitionCell( new LineCell[] {
                            new DirectiveCell( "(.*)(\"){0,1}" )
                    }, RepetitionCell.INFINITE )
            }
    );

    public static LineChecker SET_DIRECTIVE = new LineChecker(
            new LineCell[] {
                    new DirectiveCell( "\\.set" ),
                    new DirectiveCell( "([a-z]|[A-Z]|[0-9])+" ),
                    new NumberCell( NumberCell.ALL )
            }
    );

    public static LineChecker COMMENT = new LineChecker(
            new LineCell[] {
                    new RepetitionCell( new LineCell[] {
                            new CommentCell()
                    }, RepetitionCommaCell.INFINITE )
            }
    );

    public static LineChecker[] LINES = new LineChecker[] {
            ADDRESSLESS_INSTRUCTIONS,
            ADDRESS_INSTRUCTIONS,
            JUMP_INSTRUCTIONS,
            BYTE_DIRECTIVE,
            WORD_DIRECTIVE,
            REPT_DIRECTIVE,
            ENDR_DIRECTIVE,
            SECTION_DIRECTIVE,
            SKIP_DIRECTIVE,
            ASCIIZ_DIRECTIVE,
            PLACE_DIRECTIVE,
            SET_DIRECTIVE
    };

    static {
        initLines();
    }

    public static LineChecker getLineChecker( String start )
    {
        LineChecker checker = null;
        LineCell cell;
        int entry;
        boolean found = false;
        for (int i = 0; i < LINES.length && !found; i++)
        {
            checker = LINES[i];
            checker.resetIterator();
            while( true ) {
                cell = checker.getFirstCell(start);
                if ( cell.test(start) )
                    found = true;
                if (  checker.firstTourEnd() || found )
                    break;
            };
        };
        return checker;
    }

    public static void initLines() {

        ADDRESS_INSTRUCTIONS.each((i, lineCell) -> {
            if ( i.intValue() == 1 ) {
                lineCell.setBeforeEndCheck(() -> {
                    Addressing ac = AddressingCell.createAddressingByText( lineCell.getContent() );
                    if ( ac instanceof RegisterDirect ) {
                        if ( ((RegisterDirect) ac).getRegisterNumber() > 31 || ((RegisterDirect) ac).getRegisterNumber() < 0 )
                            return false;
                    }
                    return true;
                });
                lineCell.setErrorRunnable( () -> {
                    Addressing ac = AddressingCell.createAddressingByText( lineCell.getContent() );
                    if ( ac instanceof RegisterDirect ) {
                        if ( ((RegisterDirect) ac).getRegisterNumber() > 31 || ((RegisterDirect) ac).getRegisterNumber() < 0 )
                        {
                            lineCell.setErrorMessage("Allowed register number is between 0-31.");
                        }
                    }
                } );

            };
        });

        JUMP_INSTRUCTIONS.each((i, lineCell) -> {
            if ( i.intValue() == 1 ) {
                lineCell.setBeforeEndCheck(() -> {
                    Addressing a = AddressingCell.createAddressingByText( lineCell.getContent() );
                    if ( !(a instanceof MemoryDirect) || !lineCell.getContent().matches(REGEX_LABEL_NAME) ) {
                        return false;
                    }
                    return true;
                });
                lineCell.setErrorRunnable( () -> {
                    Addressing a = AddressingCell.createAddressingByText( lineCell.getContent() );
                    if ( !(a instanceof MemoryDirect) || !lineCell.getContent().matches(REGEX_LABEL_NAME) ) {
                        lineCell.setErrorMessage("Only label-memory direct addressing is allowed for the instruction " + lineCell.getPrevious().getContent().toUpperCase() + "." );
                    }
                } );

            };
        });

        SET_DIRECTIVE.each(new BiConsumer<Integer, LineCell>() {
            @Override
            public void accept(Integer i, LineCell lineCell) {
                if ( i.intValue() == 1 ) {
                    lineCell.setNewAttrSet( Styles.attrGreen );
                    lineCell.setBeforeEndCheck(() ->
                        lineCell.getContent().matches( Regex.REGEX_PROGRAM_AVAILABLE_REGISTERS )
                    );
                    lineCell.setErrorRunnable( () -> {
                        lineCell.setErrorMessage( "Register " + lineCell.getContent() + " does not exist" );
                    } );
                } else if ( i.intValue() == 2 ) {
                    lineCell.setBeforeEndCheck(() -> {
                        String s = lineCell.getPrevious().getContent();
                        Register reg = Utilities.getRegisterByName(s.toUpperCase());
                        int num = 0;
                        try {
                            num = ((NumberCell) lineCell).getContentAsInteger();
                        } catch( NumberFormatException nfe ) { return false; }

                        if ( !reg.canNumberBeWrittenByThisRegister(num) )
                            return false;

                        return true;
                    });
                    lineCell.setErrorRunnable( () -> {
                        String s = lineCell.getPrevious().getContent();
                        Register reg = Utilities.getRegisterByName(s.toUpperCase());
                        try {
                            int num = ((NumberCell) lineCell).getContentAsInteger();
                            if ( lineCell.getContent().matches("^0[xXbB][0]+(.*)$") )
                                lineCell.setErrorMessage( "Number must start with 1" );
                            else {
                                lineCell.setErrorMessage("Number " + Utilities.getHexadecimalFormat(4, num) + " cannot be written with " + reg.getSize() + " bits");
                            };
                        } catch( NumberFormatException nfe ) {
                            lineCell.setErrorMessage( "Number " + lineCell.getContent().toUpperCase() + " cannot be written with " + reg.getSize() + " bits" );
                        }

                    } );
                }
            }
        });
    }

    public static boolean checkLines() {
        int currLn = 1, ln;
        String text = MainScreenController.CurrentTab.TextPane.getText();
        String line;
        String lines[] = text.split("\\r\\n");
        ln = lines.length;
        String words[];
        int intern;
        boolean isSet;
        String substring;
        boolean test;
        boolean comment;
        boolean error;
        LinkedList<String> messages = new LinkedList<>();

        while( currLn <= ln ) {

            if ( lines[currLn-1].equals("") )
            {
                currLn++;
                continue;
            };

            words = lines[currLn-1].split("[ ]+");
            intern = 0;
            error = false;
            substring = words[intern];
            substring = substring.replaceAll("^[\\t ]+", "");
            substring = substring.replaceAll("[\\t ]*[\\r\\n]*$", "");
            substring = substring.replaceAll("[\\t ]*\\@(.*)[\\r\\n]*$", "");
            substring = substring.replaceAll("^\\@(.*)", "");
            LineChecker checker = Lines.getLineChecker( substring );
            isSet = checker != null;
            comment = false;
            if ( checker != null ) {
                checker.resetIterator();
            };
            if ( substring.equals("") )
            {
                currLn++;
                continue;
            };
            while( intern < words.length && !error ) {

                if ( comment ) {
                    break;
                }


//                if ( words[intern].equals("") || words[intern].equals("@") || words[intern].matches("^\\@(.*)" ) )
//                {
//                    if ( words[intern].matches("^\\@(.*)") ) {
//                        break;
//                    };
//                    intern++;
//                    continue;
//                };

                substring = words[intern];
                substring = substring.replaceAll("^[\\t ]+", "");
                substring = substring.replaceAll("[\\t ]*[\\r\\n]*$", "");

                if ( substring.matches("^[\\t ]+$") )
                {
                    intern++;
                    continue;
                };

                if ( substring.matches("[\\t ]*\\@") )
                {
                    comment = true;
                };
                substring = substring.replaceAll("[\\t ]*\\@(.*)[\\r\\n]*$", "");
                if ( substring.equals("") ) {
                    intern++;
                    continue;
                };
                test = true;
                // If checker is not set.
                if ( !isSet || checker == null )
                {
                    if ( substring.matches(Regex.REGEX_LABEL_DECL) ) {
                        test = false;
                    } else if ( substring.matches(Regex.REGEX_COMMENT_START) ) {
                        checker = Lines.COMMENT;
                        checker.getFirstCell(substring);
                        checker.resetIterator();
                        isSet = true;
                        test = true;
                    } else {
                        checker = Lines.getLineChecker(substring);
                        if ( checker != null )
                            checker.resetIterator();
                        isSet = true;
                        test = true;
                    }
                } else if ( checker != null ) {
                    if ( substring.matches(Regex.REGEX_LABEL_DECL) ) {
                        test = false;
                        checker = null;
                    } else if ( substring.matches(Regex.REGEX_COMMENT_START) ) {
                        checker = Lines.COMMENT;
                        checker.getFirstCell(substring);
                        checker.resetIterator();
                        test = true;
                    }
                }
                if ( checker == null )
                    break;

                checker.getCurrent().setContent( substring );
                if ( test ) {
                    checker.getCurrent().setContent(substring);
                    checker.getCurrent().testLineCell(substring);
                    if ( !checker.getCurrent().getIsValid() ) {
                        if ( intern == 0 )
                            messages.add( "Error: line " + currLn + ": Invalid instruction/directive '" + substring + "'" );
                        else {
                            checker.getCurrent().execErrorRunnable();
                            messages.add("Error: line " + currLn + ": " + checker.getCurrent().getErrorMessage());
                        };
                        error = true;
                    };

                    checker.moveToNext();
                };

                intern++;
            };

            if ( checker != null && !checker.isFinished() ) {
                messages.add( "Error: line " + currLn + ": Missing statement part(s)" );
            }
            currLn++;
        };

        if ( messages.size() > 0 ) {
            MainScreenController.appendNewLineWithText( "Error(s) occurred:" );
            Iterator<String> str = messages.iterator();
            String s;
            while (str.hasNext()) {
                s = str.next();
                MainScreenController.appendOutputText(s);
            };
            return false;
        } else {
            return true;
        }

    }

    public static void main(String[] varg)
    {
        LineChecker lc = Lines.ADDRESS_INSTRUCTIONS;
        String[] arr = { "jmp", "labela" };

        LineChecker checker = null;
        LineCell cell;
        int entry;
        boolean found = false;
        for (int i = 0; i < LINES.length && !found; i++)
        {
            checker = LINES[i];
            checker.resetIterator();
            while( true ) {
                cell = checker.getFirstCell(arr[0]);
                if (cell.test(arr[0]))
                    found = true;
                if (  checker.firstTourEnd() || found )
                    break;
            };
        };

        checker.resetIterator();
        checker.getFirstCell(arr[0]);
        boolean isSet = false;
        for (int i=0; i<arr.length; i++)
        {
            checker.getCurrent().setContent(arr[i]);
            boolean value = checker.getCurrent().testLineCell(arr[i]);
            if ( !value && checker == ADDRESSLESS_INSTRUCTIONS && arr[i].matches(REGEX_INSTRUCTION) && !isSet )
            {
                if ( arr[i].matches(REGEX_ADDRESS_INSTRUCTIONS ) )
                    checker = ADDRESS_INSTRUCTIONS;
                else if ( arr[i].matches(REGEX_JUMP_INSTRUCTIONS) )
                    checker = JUMP_INSTRUCTIONS;

                checker.resetIterator();
                checker.getFirstCell(arr[i]);
                checker.getCurrent().setContent(arr[i]);
                value = checker.getCurrent().testLineCell(arr[i]);
                isSet = true;
            };
            checker.moveToNext();

            System.out.println(value);
        }


//        lc.getFirstCell("labela:");
//        lc.getCurrent().setContent("labela:");
//        System.out.println( lc.getCurrent().testLineCell("labela:") );
//
//        lc.moveToNext();
//        lc.getCurrent().setContent("ST");
//        System.out.println( lc.getCurrent().testLineCell("ST") );
//
//        lc.moveToNext();
//        lc.getCurrent().setContent("[r0]");
//        System.out.println( lc.getCurrent().testLineCell("[r0]") );
//
//        lc.getCurrent().finishTesting();
//        System.out.println( lc.getCurrent().getExplanation() );
    }
}
