#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(int argc, char* argv[]){
  printf("----TESTING PARAMETER INPUT----\n");
  
  printf("Printing given input arguments\n");

  int i = 0;
  while(i < argc){
    printf("Arg %d: %s\n",i,argv[i]);
    i++;
  }
  printf("\n");
  
  return 1;
}
