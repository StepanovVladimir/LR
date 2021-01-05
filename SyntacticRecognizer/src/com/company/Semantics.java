package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Semantics
{
    public static class Type
    {
        public Type(String name)
        {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Type type = (Type) o;
            return name.equals(type.name) &&
                    (arrSize == null && type.arrSize == null || arrSize != null && type.arrSize != null && arrSize.equals(type.arrSize));
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, arrSize);
        }

        public String name;
        public Integer arrSize = null;
    }

    public Semantics(Stack<String> charsStack, String fileName) throws IOException
    {
        this.charsStack = charsStack;
        writer = new FileWriter(fileName);
    }

    private Stack<String> charsStack;
    private FileWriter writer;
    private Stack<Type> typesStack = new Stack<>();
    private ArrayList<Map<String, Type>> charsTable = new ArrayList<>();
    private Map<String, ArrayList<Type>> functionsTable = new TreeMap<>();
    private ArrayList<Type> functionParameters;
    private String calledFunctionName;
    private ArrayList<Type> calledFunctionParameters;
    private Stack<String> operations = new Stack<>();
    private String comparison;
    private int labelNumber = 0;
    private Stack<Integer> labelNumbers = new Stack<>();

    public void doFunction(String function) throws Exception
    {
        String id;
        Type type1;
        Type type2;
        ArrayList<Type> params;
        switch (function)
        {
            case "enteringBlock":
                charsTable.add(new TreeMap<>());
                break;

            case "exitBlock":
                charsTable.remove(charsTable.size() - 1);
                writer.flush();
                break;

            case "ifLabelJZ":
                writer.write("label" + labelNumber);
                writer.write('\n');
                writer.write("-jz\n");
                labelNumbers.push(labelNumber);
                labelNumber += 2;
                break;

            case "elseProcess":
                writer.write("label" + (labelNumbers.peek() + 1));
                writer.write('\n');
                writer.write("-jmp\n");
                writer.write("label" + labelNumbers.peek());
                writer.write('\n');
                writer.write("-defl\n");
                break;

            case "endElse":
                writer.write("label" + (labelNumbers.pop() + 1));
                writer.write('\n');
                writer.write("-defl\n");
                break;

            case "defLabelAtWhile":
                writer.write("label" + labelNumber);
                writer.write('\n');
                writer.write("-defl\n");
                labelNumbers.push(labelNumber);
                labelNumber += 2;
                break;

            case "whileLabelJZ":
                writer.write("label" + (labelNumbers.peek() + 1));
                writer.write('\n');
                writer.write("-jz\n");
                break;

            case "endWhile":
                int ln = labelNumbers.pop();
                writer.write("label" + ln);
                writer.write('\n');
                writer.write("-jmp\n");
                writer.write("label" + (ln + 1));
                writer.write('\n');
                writer.write("-defl\n");
                break;

            case "pushTypeVoid":
                typesStack.push(new Type("void"));
                break;

            case "pushTypeInt":
                typesStack.push(new Type("int"));
                break;

            case "pushTypeBool":
                typesStack.push(new Type("bool"));
                break;

            case "pushTypeChar":
                typesStack.push(new Type("char"));
                break;

            case "pushTypeString":
                typesStack.push(new Type("string"));
                break;

            case "pushTypeFloat":
                typesStack.push(new Type("float"));
                break;

            case "pushTypeIntAndPrint":
                typesStack.push(new Type("int"));
                writer.write(charsStack.elementAt(charsStack.size() - 1));
                writer.write('\n');
                break;

            case "pushTypeBoolAndOr":
                typesStack.push(new Type("bool"));
                writer.write("||\n");
                break;

            case "pushTypeBoolAndAnd":
                typesStack.push(new Type("bool"));
                writer.write("&&\n");
                break;

            case "pushTypeCharAndPrint":
                typesStack.push(new Type("char"));
                writer.write(charsStack.elementAt(charsStack.size() - 1));
                writer.write('\n');
                break;

            case "pushTypeStringAndPrint":
                typesStack.push(new Type("string"));
                writer.write(charsStack.elementAt(charsStack.size() - 1));
                writer.write('\n');
                break;

            case "pushTypeStringAndConcat":
                typesStack.push(new Type("string"));
                writer.write(".\n");
                break;

            case "pushTypeFloatAndPrint":
                typesStack.push(new Type("float"));
                writer.write(charsStack.elementAt(charsStack.size() - 1));
                writer.write('\n');
                break;

            case "printConcat":
                writer.write(".\n");
                break;

            case "printOr":
                writer.write("||\n");
                break;

            case "printAnd":
                writer.write("&&\n");
                break;

            case "printNot":
                writer.write("!\n");
                break;

            case "checkIsNotArray":
                type1 = typesStack.pop();
                if (type1.arrSize != null)
                {
                    throw new Exception("Cannot print array");
                }
                writer.write("-print\n");
                break;

            case "checkIsReadable":
                type1 = typesStack.pop();
                if (type1.arrSize != null || !type1.name.equals("char") && !type1.name.equals("int") && !type1.name.equals("float"))
                {
                    throw new Exception("Cannot read this type");
                }
                writer.write("-read\n");
                break;

            case "specifyArrSize":
                typesStack.peek().arrSize = Integer.parseInt(charsStack.elementAt(charsStack.size() - 1));
                break;

            case "addFuncType":
                functionParameters = new ArrayList<>();
                functionParameters.add(typesStack.pop());
                break;

            case "addFuncTypeVoid":
                functionParameters = new ArrayList<>();
                functionParameters.add(new Type("void"));
                break;

            case "addFunction":
                id = charsStack.elementAt(charsStack.size() - 1);
                if (!functionsTable.containsKey(id))
                {
                    functionsTable.put(id, functionParameters);
                    writer.write(id);
                    writer.write('\n');
                    writer.write(functionParameters.get(0).name);
                    writer.write('\n');
                    writer.write("-funcdecl\n");
                    writer.write("-argsdeclbeg\n");
                }
                else
                {
                    throw new Exception("Double Declaration of " + id + " function");
                }
                break;

            case "addFunctionMain":
                id = charsStack.elementAt(charsStack.size() - 1);
                if (!functionsTable.containsKey(id))
                {
                    functionsTable.put(id, functionParameters);
                    writer.write("-main\n");
                    writer.write("-blbeg\n");
                }
                else
                {
                    throw new Exception("Double Declaration of " + id + " function");
                }
                break;

            case "addArgument":
                id = charsStack.elementAt(charsStack.size() - 1);
                type1 = typesStack.pop();
                if (!charsTable.get(charsTable.size() - 1).containsKey(id))
                {
                    charsTable.get(charsTable.size() - 1).put(id, type1);
                }
                else
                {
                    throw new Exception("Double Declaration of " + id + " variable");
                }
                functionParameters.add(type1);
                writer.write(id);
                writer.write('\n');
                writer.write(type1.name);
                writer.write('\n');
                if (type1.arrSize == null)
                {
                    writer.write("-argdecl\n");
                }
                else
                {
                    writer.write(type1.arrSize.toString());
                    writer.write('\n');
                    writer.write("-arrargdecl\n");
                }
                break;

            case "enteringFunc":
                writer.write("-argsdeclend\n");
                writer.write("-blbeg\n");
                break;

            case "exitFunc":
                writer.write("-blend\n");
                break;

            case "addToCharsTable":
                id = charsStack.elementAt(charsStack.size() - 1);
                if (!charsTable.get(charsTable.size() - 1).containsKey(id))
                {
                    type1 = typesStack.pop();
                    charsTable.get(charsTable.size() - 1).put(id, type1);
                    writer.write(id);
                    writer.write('\n');
                    writer.write(type1.name);
                    writer.write('\n');
                    if (type1.arrSize == null)
                    {
                        writer.write("-vardecl\n");
                    }
                    else
                    {
                        writer.write(type1.arrSize.toString());
                        writer.write('\n');
                        writer.write("-arrdecl\n");
                    }
                }
                else
                {
                    throw new Exception("Double Declaration of " + id + " variable");
                }
                break;

            case "findFunc":
                id = charsStack.elementAt(charsStack.size() - 2);
                if (functionsTable.containsKey(id))
                {
                    calledFunctionName = id;
                    calledFunctionParameters = new ArrayList<>();
                    writer.write("-argsbeg\n");
                }
                else
                {
                    throw new Exception("No " + id + " function");
                }
                break;

            case "addTypeToCalledFunc":
                calledFunctionParameters.add(typesStack.pop());
                writer.write("-arg\n");
                break;

            case "checkParameters":
                params = functionsTable.get(calledFunctionName);
                if (calledFunctionParameters.equals(params.subList(1, params.size())))
                {
                    typesStack.push(params.get(0));
                    writer.write("-argsend\n");
                }
                else
                {
                    throw new Exception("Invalid params to " + calledFunctionName + "function");
                }
                break;

            case "checkParametersWithoutPush":
                params = functionsTable.get(calledFunctionName);
                if (!calledFunctionParameters.equals(params.subList(1, params.size())))
                {
                    throw new Exception("Invalid params to " + calledFunctionName + "function");
                }
                writer.write("-argsend\n");
                break;

            case "checkFuncType":
                if (!functionParameters.get(0).equals(typesStack.pop()))
                {
                    throw new Exception("Returned parameter not equals to function type");
                }
                writer.write("-retval\n");
                break;

            case "checkFuncIsVoid":
                if (!functionParameters.get(0).name.equals("void"))
                {
                    throw new Exception("Function not have returned value");
                }
                writer.write("-return\n");
                writer.write(";\n");
                break;

            case "endStmt":
                writer.write(";\n");
                break;

            case "print":
                writer.write(charsStack.elementAt(charsStack.size() - 1));
                writer.write('\n');
                break;

            case "pushTypeOfId1":
                id = charsStack.elementAt(charsStack.size() - 2);
                for (int i = charsTable.size() - 1; i >= 0; i--)
                {
                    if (charsTable.get(i).containsKey(id))
                    {
                        type1 = charsTable.get(i).get(id);
                        if (type1.arrSize == null)
                        {
                            if (!typesStack.empty() && typesStack.peek().name.equals("arr"))
                            {
                                throw new Exception(id + " is not array");
                            }
                            typesStack.push(type1);
                        }
                        else
                        {
                            if (!typesStack.empty() && typesStack.peek().name.equals("arr"))
                            {
                                typesStack.pop();
                                typesStack.push(new Type(type1.name));
                            }
                            else
                            {
                                typesStack.push(type1);
                            }
                        }
                        return;
                    }
                }
                throw new Exception("No " + id + " variable");

            case "pushTypeOfId2":
                id = charsStack.elementAt(charsStack.size() - 3);
                for (int i = charsTable.size() - 1; i >= 0; i--)
                {
                    if (charsTable.get(i).containsKey(id))
                    {
                        type1 = charsTable.get(i).get(id);
                        if (type1.arrSize == null)
                        {
                            if (!typesStack.empty() && typesStack.peek().name.equals("arr"))
                            {
                                throw new Exception(id + " is not array");
                            }
                            typesStack.push(type1);
                        }
                        else
                        {
                            if (!typesStack.empty() && typesStack.peek().name.equals("arr"))
                            {
                                typesStack.pop();
                                typesStack.push(new Type(type1.name));
                            }
                            else
                            {
                                typesStack.push(type1);
                            }
                        }
                        return;
                    }
                }
                throw new Exception("No " + id + " variable");

            case "pushArr":
                typesStack.push(new Type("arr"));
                writer.write("-subs\n");
                break;

            case "saveOperation":
                operations.push(charsStack.elementAt(charsStack.size() - 1));
                break;

            case "saveComparison":
                comparison = charsStack.elementAt(charsStack.size() - 1);
                break;

            case "printUnaryMinus":
                writer.write("~\n");
                break;

            case "checkTypesAtAssign":
                type2 = typesStack.pop();
                type1 = typesStack.pop();
                if (type1.arrSize != null || type2.arrSize != null || !type1.name.equals(type2.name) && !type1.name.equals("float") && !type2.name.equals("int"))
                {
                    throw new Exception("Invalid types at assign");
                }
                writer.write("=\n");
                break;

            case "checkTypeIsString":
                type1 = typesStack.pop();
                if (type1.arrSize != null || !type1.name.equals("string"))
                {
                    throw new Exception("Variable is not string");
                }
                break;

            case "checkTypeIsIntOrFloat":
                if (typesStack.peek().arrSize != null || !typesStack.peek().name.equals("int") && !typesStack.peek().name.equals("float"))
                {
                    throw new Exception("Variable is not int or float");
                }
                break;

            case "pushTypeIntOrFloatFrom2":
                type1 = typesStack.pop();
                type2 = typesStack.pop();
                if (type1.name.equals("float") || type2.name.equals("float"))
                {
                    typesStack.push(new Type("float"));
                }
                else
                {
                    typesStack.push(new Type("int"));
                }
                writer.write(operations.pop());
                writer.write('\n');
                break;

            case "checkTypeIsBool":
                type1 = typesStack.pop();
                if (type1.arrSize != null || !type1.name.equals("bool"))
                {
                    throw new Exception("Variable is not bool");
                }
                break;

            case "checkTypeIsChar":
                type1 = typesStack.pop();
                if (type1.arrSize != null || !type1.name.equals("char"))
                {
                    throw new Exception("Variable is not char");
                }
                break;

            case "checkTypeIsInt":
                type1 = typesStack.pop();
                if (type1.arrSize != null || !type1.name.equals("int"))
                {
                    throw new Exception("Variable is not int");
                }
                break;

            case "checkTypesAtComparison":
                type1 = typesStack.pop();
                type2 = typesStack.pop();
                if (type1.arrSize != null || type2.arrSize != null || (!type1.name.equals("char") || !type2.name.equals("char")) &&
                        (!type1.name.equals("int") && !type1.name.equals("float") || !type2.name.equals("int") && !type2.name.equals("float")))
                {
                    throw new Exception("Invalid types at comparison");
                }

                writer.write(comparison);
                writer.write('\n');
                break;
        }
    }
}
