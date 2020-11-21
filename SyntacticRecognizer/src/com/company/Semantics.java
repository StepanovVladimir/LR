package com.company;

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

    public Semantics(Stack<String> charsStack)
    {
        this.charsStack = charsStack;
    }

    private Stack<String> charsStack;
    private Stack<Type> typesStack = new Stack<>();
    private ArrayList<Map<String, Type>> charsTable = new ArrayList<>();
    private Map<String, ArrayList<Type>> functionsTable = new TreeMap<>();
    private ArrayList<Type> functionParameters;
    private String calledFunctionName;
    private ArrayList<Type> calledFunctionParameters;

    public void doFunction(String function) throws Exception {
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

            case "popType":
                typesStack.pop();
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
                break;

            case "addToCharsTable":
                id = charsStack.elementAt(charsStack.size() - 1);
                if (!charsTable.get(charsTable.size() - 1).containsKey(id))
                {
                    charsTable.get(charsTable.size() - 1).put(id, typesStack.pop());
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
                }
                else
                {
                    throw new Exception("No " + id + " function");
                }
                break;

            case "addTypeToCalledFunc":
                 calledFunctionParameters.add(typesStack.pop());
                 break;

            case "checkParameters":
                params = functionsTable.get(calledFunctionName);
                if (calledFunctionParameters.equals(params.subList(1, params.size())))
                {
                    typesStack.push(params.get(0));
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
                break;

            case "checkFuncType":
                if (!functionParameters.get(0).equals(typesStack.pop()))
                {
                    throw new Exception("Returned parameter not equals to function type");
                }
                break;

            case "checkFuncIsVoid":
                if (!functionParameters.get(0).name.equals("void"))
                {
                    throw new Exception("Function not have returned value");
                }
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
                break;

            case "checkTypesAtAssign":
                type1 = typesStack.pop();
                type2 = typesStack.pop();
                if (type1.arrSize != null || type2.arrSize != null || !type1.name.equals(type2.name) && !type1.name.equals("float") && !type2.name.equals("int"))
                {
                    throw new Exception("Invalid types at assign");
                }
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
                break;
        }
    }
}
