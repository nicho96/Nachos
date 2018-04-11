#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(int argc, char* argv[]){
    printf("----TESTING JOIN----\n");
    
    printf("Starting process hello.coff\n");
    int pid = exec("hello.coff", 0, null);
    int status;
    join(pid, &status);
    printf("After hello.coff join\n");
    return 0;
}
