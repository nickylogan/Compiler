# SIMPLE COMPILER

## Syntax

```php
// comment syntax

// variable declaration
var varName
var arrName[length_in_number]

// initialization
var varName = arithmetic_expression

// assignment
varName = arithmetic_expression
arrName[index_in_number] = arithmetic_expression

// arithmetic expression
// complex ones are supported, e.g. 5+3*(a+b)
// supported operators: + - * /

// if without else
if(condition)
  // code
endif

// if with else
if(condition)
  // code
else
  // code
endif

// while
while(condition)
// code
endwhile

// boolean expression
// complex boolean expression supported, e.g.
// !(a>b)||(a != b-1) && (5+3>=10)
// supported logical operators: || && !
// supported comparison operators: < <= > >= == !=
```

## Limitations

* `else if` not supported
* `for` and `do while` unsupported
* array initializer list unsupported
* negative numbers unsupported
* basic operators such as `++ -- %` unsupported
* functions unsupported
* ternary operator not supported
* `string` unsupported
* all data types are 4 byte `int`