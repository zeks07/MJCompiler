# MJCompiler

This project is a compiler for the MicroJava programming language, developed as part of 
the Compiler Construction 1 course. It translates MicroJava source code into custom bytecode that can
be executed on a specialized MicroJava Runtime.

### Overview

The compiler is built using a multiphase architecture:
- Lexical analysis (with JFlex)
- Syntax analysis (with CUP)
- Semantic analysis
- Code generation 

### MicroJava language

#### Data Types & Structures
- Primitive types: `int`, `char`, `bool`
- Enums
- One dimensional arrays
- Classes and inheritance

#### Control Flow & Operators
- Operations: Arithmetic, Relational, Logical
- Ternary operator
- `if`, `switch` and `for` statements

#### Built-in Functions
- I/O: `print()` and `read()`
- Utility: `len()`, `ord()` and `chr()`

#### Language example

```
program SyntaxExample

abstract class Shape 
{
    int x, y;
    {
        abstract int area();
        void move(int dx, int dy)
        {
            x = x + dx;
            y = y + dy;
        }
    }
}

class Square extends Shape
{
    int side;
    {
        int area()
        {
            return side * side;
        }
    }
}

{
    void main() 
        Square s;
        int i;
    {
        s = new Square;
        s.side = 5;
        s.move(10, 10);
        
        print(s.area());
        print(eol);
    }
}

```

The result bytecode with readable instructions can be seen [here](src/tests/testData/res.txt).
