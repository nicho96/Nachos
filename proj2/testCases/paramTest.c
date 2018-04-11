#include "stdio.h"
int main(int argc, char* argv[]){
  printf("----TESTING PARAMETER INPUT----\n");
  
  printf("Printing given input arguments\n");
  
  int i = 0;
  while(i < argc){
    printf("Arg %n: %s\n",i,argv[i]);
    i++;
  }
  printf("\n");
  
  return 1;
}