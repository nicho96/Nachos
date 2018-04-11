#include "stdio.h"
#include "stdlib.h"
int main(int argc, char* argv[]){
  printf("----TESTING EXIT----\n");
  printf("Passing 42 to exit system call\n");
  exit(42);
  printf("Failure: exit did not occur");
}