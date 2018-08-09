# SIMPLE COMPILER

A simple compiler for a custom language and computer simulator. This project is only made for educational use, to roughly demonstrate how a program code is compiled and ran in the computer. However, the inner workings of the compiler *does not* represent a real-world compiler -- it is just enough to translate a program code into machine code.

## Usage

To run the program, simply [download](https://github.com/Log-baseE/Compiler/releases/download/v1.0-beta/compiler.jar) the provided release and run it. You can also run the following command:

```sh
# Make sure that the jar is present in the current working directory
$ java -jar compiler.jar
```

Instructions to build the project are currently not available.

## User Interface

### Code editor

![Code editor interface screenshot](https://i.imgur.com/8yB6mPQ.jpg)

#### *Menu bar*

| Shortcut       | Menu                          | Description                                  |
| -------------- | ----------------------------- | -------------------------------------------- |
| `Ctrl+N`       | File >New file                | New program file                             |
| `Ctrl+O`       | File > Open file              | Open a program `.cpr` file                   |
| `Ctrl+S`       | File > Save file              | Save current program file                    |
| `Ctrl+Shift+S` | File > Save file as...        | Save current program as a `.cpr` file        |
| `Shift+F5`     | Compile > Compile code        | Compile the current program                  |
| `F5`           | Compile > Run code            | Run the current program (will open debugger) |
| `Alt+1`        | View > Instruction tab        | Toggle instruction tab                       |
| `Alt+2`        | View > Machine code (hex) tab | Toggle machine code (hex) tab                |
| `Alt+3`        | View > Machine code (dec) tab | Toggle machine code (dec) tab                |
| `F1`           | Help > Help                   | Open help menu                               |
| -              | Help > About                  | Open about                                   |

#### *Editor tabs*

The first tab is the code editor. Once the program is compiled, the rest of the tabs will contain the compilation result.

![Instruction screenshot](https://i.imgur.com/m3uWNui.jpg)
![Machine code hex screenshot](https://i.imgur.com/GhhEofq.jpg)
![Machine code dec screenshot](https://i.imgur.com/mUo71lx.jpg)

### Debugger

![Debugger screenshot](https://i.imgur.com/ehRtjpf.jpg)

| Number | Description                                                                                                                     |
| ------ | ------------------------------------------------------------------------------------------------------------------------------- |
| 1      | Compiled code                                                                                                                   |
| 2      | Symbol table                                                                                                                     |
| 3      | Locator: will search for the queried **decimal** location in the memory, and will show it in the memory viewer                   |
| 4      | Memory viewer                                                                                                                   |
| 5      | Next button: will step to next instruction. *Once clicked*, holding `Enter` will fast forward program execution until released/finished. |

## Syntax Reference

### Single variables

Valid variable identifiers must match the regex `[A-Za-z_][A-Za-z0-9_]*`. It is important to note that all of these can only contain **4-byte unsigned integers**.

#### *Instantiation*

This tells the program that the given variable exists. Doin so allocates 4 bytes of memory, and adds the identifier to the symbol table.

```php
var <identifier>

// For example:
var abcd123
var _abc

// Invalid syntax:
var 123abc
```

On the current version, shorthand for multiple instantiations is not supported.

```php
// Generates compile error
var var1, var2, var3
```

#### *Assignment and initialization*

Once a variable is instantiated, its content can be changed later in the program. An undeclared variable will of course generate a compile error, since it is not found on the symbol table.

The usage of **complex arithmetic expressions** on the right-hand side of an assignment is supported.

> Supported operations in arithmetic: `+ - * /`

```php
<varname> = <arithmetic>

var a
var b
a = 5 + 7
b = a + 10
```

The compiler also supports **variable initialization**.

```php
var <varname> = <arithmetic>
var c = 10 + a * (7 - b) / 2  // assuming the variables declared in the previous
                              // example are in the same program
```

Once a variable is declared, it cannot be redeclared later (this will generate a compile error!).

```php
var abcd = 5
var abcd // compile error!
```

### Arrays

The compiler supports the usage of arrays.

#### *Declaration*

```php
var <arrname>[<length_in_number>]
// For example:
var arr[10]
```

Array declaration is still limited, causing the following examples to generate compile errors.

```php
// All of these generate compile errors
var arr[]         // length must be specified
var arr[10 + 5]   // length should strictly be a number

var a = 5
var arr[a]        // length cannot be inferred from another variable
```

#### *Initialization and assignment + limitations*

Just like variable assignment, an array element can be assigned with the return value of an **arithmetic expression**. However, accessing an array can only be done using a **strictly numerical index**.

```php
<arrname>[<index-in-number>] = <arithmetic>

var arr[10]
var a = 10
var b = 1
arr[0] = 10 + 5 * (a - 3 * b + arr[1])

// These will generate compile errors
arr[1 + 2] = 5  // usage of arithmetic as index
arr[b] = 5      // usage of variable as index
```

***Unfortunately***, unlike single variables, **initializing an array** with a list is ***not*** supported.

```php
var a[5] = {1, 2, 3, 4, 5}  // not supported
```

#### *Accessing an element*

An array element can be accessed as a part of an arithemtic expression. However, just like before, they can only be accessed with a **strictly numerical index**.

```php
var arr[5]
var b = 3
arr[0] = b
var arr[1] = arr[0] * 5

// These will generate compile errors
var c = arr[1 + 2]  // usage of arithmetic as index
var d = arr[arr[0]] // usage of array element as index
var e = arr[b]      // usage of variable as index
```

### Control flow

The compiler supports complex **boolean expressions**. These can be put as a condition for `if/else` or `while` statements.

```php
var a = 5
var b = 7

!(a + b > 5) && false || (true == false) && (5 <= b) && (5 != 6 + 7)
```

Supported logical operations include:

- Logical: negation `!`, conjunction `&&`, disjunction `||`
- Comparison: `< > <= >= == !=`

#### *Conditional - `if/else`*

The compiler supports `if/else` statements.

```php
if (<condition>)
  // code
endif

if (<condition>)
  // code
else
  // code
endif
```

*Limitation*: `else if` statements not supported

#### *Loops - `while`*

The compiler also supports `while` statements.

```php
while(<condition>)
  // code
endwhile
```

### Other Limitations

The compiler is very much limited:

- `for` and `do while` unsupported
- negative numbers unsupported
- basic operators such as `++ -- %` unsupported
- functions unsupported
- ternary operator not supported
- `string` unsupported
- all data types are 4 byte unsigned `int`


## Authors

- [**Nicky Logan**](https://github.com/Log-baseE/)
- [**Madeleine J.**](https://github.com/haysacks)
- [**Nadya F. Bachtiar**](https://github.com/Ao-Re)
- [**Barjuan Davis**](https://github.com/cokpsz/)

See the list of contributors for this project [here](https://github.com/Log-baseE/Compiler/contributors)

## Acknowledgements

- **Dr. Sutrisno, S.E., M.Kom.** for the computer simulator code
- All external dependencies used in this project
