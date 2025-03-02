import java.util.Stack;
import java.util.ArrayList;

/**
 * A simple stack-based virtual machine (VM) that executes a small set of instructions.
 * It uses a stack to manage data and supports basic arithmetic, printing, and control flow.
 * Programs are written as bytecode (integer arrays) or assembled from text instructions.
 *
 * <p>The VM can run in debug mode to print the program counter (PC), current instruction,
 * and stack state at each step.</p>
 *
 * <p>Example usage:
 * <pre>
 * StackVM vm = new StackVM(true); // Debug mode on
 * String[] program = {"PUSH 3", "PRINT", "HALT"};
 * int[] bytecode = StackVM.Assembler.assemble(program);
 * vm.loadProgram(bytecode);
 * vm.run(); // Prints: PRINT: 3
 * </pre>
 * </p>
 */
public class StackVM {
    private int[] memory;           // Program memory (bytecode)
    private Stack<Integer> stack;   // Operand stack
    private int pc;                 // Program counter
    private boolean running;        // Execution flag
    private boolean debug;          // Debug mode flag
    private static final int MAX_STACK_SIZE = 100; // Stack limit

    /**
     * Creates a new StackVM instance.
     *
     * @param debug If true, prints execution details (PC, instruction, stack) for debugging.
     */
    public StackVM(boolean debug) {
        stack = new Stack<>();
        pc = 0;
        running = false;
        this.debug = debug;
    }

    /**
     * Loads a program into the VM's memory.
     *
     * @param program The bytecode array to execute (e.g., [0, 5, 6, 5] for "PUSH 5, PRINT, HALT").
     */
    public void loadProgram(int[] program) {
        if (program == null || program.length == 0) {
            System.out.println("Error: Invalid program");
            return;
        }
        this.memory = program;
        pc = 0;
        stack.clear();
    }

    /**
     * Runs the loaded program until it halts or an error occurs.
     * In debug mode, prints the PC, instruction, and stack at each step.
     */
    public void run() {
        if (memory == null) {
            System.out.println("Error: No program loaded");
            return;
        }
        running = true;
        while (running && pc < memory.length) {
            int instruction = memory[pc];
            if (debug) {
                System.out.println("PC: " + pc + ", Instruction: " + instruction + ", Stack: " + stack);
            }
            execute(instruction);
            // No pc++ here; handled in execute
        }
        if (running) {
            System.out.println("Warning: Program ended without HALT");
        }
    }

    /**
     * Executes a single instruction from the bytecode.
     * Instructions include:
     * <ul>
     *   <li>0: PUSH &lt;value&gt; - Pushes a value onto the stack</li>
     *   <li>1: POP - Removes the top value from the stack</li>
     *   <li>2: ADD - Adds top two values, pushes result</li>
     *   <li>3: SUB - Subtracts top from second, pushes result</li>
     *   <li>4: MUL - Multiplies top two values, pushes result</li>
     *   <li>5: HALT - Stops the program</li>
     *   <li>6: PRINT - Prints the top value</li>
     *   <li>7: DUP - Duplicates the top value</li>
     *   <li>8: DIV - Divides second by top, pushes result</li>
     *   <li>9: IF_EQ &lt;address&gt; - Jumps to address if top two values are equal</li>
     *   <li>10: JUMP &lt;address&gt; - Jumps to address</li>
     * </ul>
     *
     * @param instruction The instruction code to execute.
     */
    private void execute(int instruction) {
        switch (instruction) {
            case 0: // PUSH <value>
                if (pc + 1 < memory.length) {
                    pc++;
                    if (stack.size() < MAX_STACK_SIZE) {
                        stack.push(memory[pc]);
                        pc++; // Increment for next instruction
                    } else {
                        System.out.println("Error: Stack overflow at position " + pc);
                        running = false;
                    }
                } else {
                    System.out.println("Error: PUSH at position " + (pc) + " missing value");
                    running = false;
                }
                break;
            case 1: // POP
                if (!stack.isEmpty()) {
                    stack.pop();
                    pc++;
                } else {
                    System.out.println("Error: Stack underflow on POP at position " + pc);
                    running = false;
                }
                break;
            case 2: // ADD
                if (stack.size() >= 2) {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a + b);
                    pc++;
                } else {
                    System.out.println("Error: Not enough operands for ADD at position " + pc);
                    running = false;
                }
                break;
            case 3: // SUB
                if (stack.size() >= 2) {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a - b);
                    pc++;
                } else {
                    System.out.println("Error: Not enough operands for SUB at position " + pc);
                    running = false;
                }
                break;
            case 4: // MUL
                if (stack.size() >= 2) {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a * b);
                    pc++;
                } else {
                    System.out.println("Error: Not enough operands for MUL at position " + pc);
                    running = false;
                }
                break;
            case 5: // HALT
                running = false;
                break; // No pc++ needed
            case 6: // PRINT
                if (!stack.isEmpty()) {
                    System.out.println("PRINT: " + stack.peek());
                    pc++;
                } else {
                    System.out.println("Error: Stack empty for PRINT at position " + pc);
                    running = false;
                }
                break;
            case 7: // DUP
                if (!stack.isEmpty()) {
                    if (stack.size() < MAX_STACK_SIZE) {
                        stack.push(stack.peek());
                        pc++;
                    } else {
                        System.out.println("Error: Stack overflow on DUP at position " + pc);
                        running = false;
                    }
                } else {
                    System.out.println("Error: Stack empty for DUP at position " + pc);
                    running = false;
                }
                break;
            case 8: // DIV
                if (stack.size() >= 2) {
                    int b = stack.pop();
                    if (b != 0) {
                        int a = stack.pop();
                        stack.push(a / b);
                        pc++;
                    } else {
                        System.out.println("Error: Division by zero at position " + pc);
                        running = false;
                    }
                } else {
                    System.out.println("Error: Not enough operands for DIV at position " + pc);
                    running = false;
                }
                break;
            case 9: // IF_EQ <address>
                if (stack.size() >= 2) {
                    int b = stack.pop();
                    int a = stack.pop();
                    if (pc + 1 < memory.length) {
                        pc++;
                        int address = memory[pc];
                        if (a == b && address >= 0 && address < memory.length) {
                            pc = address; // Jump directly
                        } else {
                            pc++; // No jump, move to next
                        }
                    } else {
                        System.out.println("Error: IF_EQ at position " + (pc) + " missing address");
                        running = false;
                    }
                } else {
                    System.out.println("Error: Not enough operands for IF_EQ at position " + pc);
                    running = false;
                }
                break;
            case 10: // JUMP <address>
                if (pc + 1 < memory.length) {
                    pc++;
                    int address = memory[pc];
                    if (address >= 0 && address < memory.length) {
                        pc = address; // Jump directly
                    } else {
                        System.out.println("Error: Invalid JUMP address " + address + " at position " + (pc));
                        running = false;
                    }
                } else {
                    System.out.println("Error: JUMP at position " + (pc) + " missing address");
                    running = false;
                }
                break;
            default:
                System.out.println("Error: Unknown instruction " + instruction + " at position " + pc);
                running = false;
                break;
        }
    }

    /**
     * A utility class to convert text instructions into bytecode for the StackVM.
     *
     * <p>Example:
     * <pre>
     * String[] code = {"PUSH 5", "PRINT", "HALT"};
     * int[] bytecode = Assembler.assemble(code); // Returns [0, 5, 6, 5]
     * </pre>
     * </p>
     */
    public static class Assembler {
        /**
         * Converts an array of text instructions into a bytecode array.
         * Supported instructions: PUSH &lt;value&gt;, POP, ADD, SUB, MUL, HALT, PRINT, DUP, DIV,
         * IF_EQ &lt;address&gt;, JUMP &lt;address&gt;.
         *
         * @param instructions Array of instruction strings (e.g., "PUSH 3", "PRINT").
         * @return The bytecode array, or null if an error occurs.
         */
        public static int[] assemble(String[] instructions) {
            ArrayList<Integer> bytecode = new ArrayList<>();
            for (String line : instructions) {
                String[] parts = line.trim().split("\\s+");
                switch (parts[0].toUpperCase()) {
                    case "PUSH":
                        bytecode.add(0);
                        bytecode.add(Integer.parseInt(parts[1]));
                        break;
                    case "POP":
                        bytecode.add(1);
                        break;
                    case "ADD":
                        bytecode.add(2);
                        break;
                    case "SUB":
                        bytecode.add(3);
                        break;
                    case "MUL":
                        bytecode.add(4);
                        break;
                    case "HALT":
                        bytecode.add(5);
                        break;
                    case "PRINT":
                        bytecode.add(6);
                        break;
                    case "DUP":
                        bytecode.add(7);
                        break;
                    case "DIV":
                        bytecode.add(8);
                        break;
                    case "IF_EQ":
                        bytecode.add(9);
                        bytecode.add(Integer.parseInt(parts[1]));
                        break;
                    case "JUMP":
                        bytecode.add(10);
                        bytecode.add(Integer.parseInt(parts[1]));
                        break;
                    default:
                        System.out.println("Assembler error: Unknown instruction " + parts[0]);
                        return null;
                }
            }
            int[] result = new int[bytecode.size()];
            for (int i = 0; i < bytecode.size(); i++) {
                result[i] = bytecode.get(i);
            }
            return result;
        }
    }

    public static void main(String[] args) {
        StackVM vm = new StackVM(true); // Debug mode on

        // Corrected test program: Countdown from 3 with proper stack management
        String[] program = {
                "PUSH 3",      // 0: Push counter
                "DUP",         // 2: Duplicate counter
                "PRINT",       // 3: Print current value
                "POP",         // 4: Remove printed value
                "PUSH 1",      // 5: Push 1
                "SUB",         // 7: Decrement counter
                "DUP",         // 8: Duplicate new counter
                "PUSH 0",      // 9: Push 0
                "IF_EQ 15",    // 11: If counter == 0, jump to HALT (position 15)
                "JUMP 2",      // 13: Jump back to DUP (print loop)
                "HALT"         // 15: End
        };
        int[] bytecode = Assembler.assemble(program);
        if (bytecode != null) {
            vm.loadProgram(bytecode);
            vm.run();
            System.out.println("Final stack: " + vm.stack);
        }
    }
}
