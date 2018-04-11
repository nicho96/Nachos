#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"
int main(int argc, char* argv[]){
    printf("----TESTING EXEC----\n");
    
    char* input[] = {"Bonjour","Au Revoir"};
    printf("Sending the inputs: %s, %s\n",input[0],input[1]);
    int result = exec("paramTest.coff",2,input);
    
    printf("Checking whether exec was called succesfully\n");
    if(result == 0){
      printf("Success: exec called\n\n");
      return 0;
    }
    printf("Failure: exec failed\n\n");
    return -1;
}