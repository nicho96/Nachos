#include "stdio.h"
int main(int argc, char* argv[]){
  printf("----TESTING CREATE----\n");
  
  printf("Creating a file called \"creatTestFile\"\n");
  int fileDescriptor = creat("creatTestFile");
  
  printf("Checking whether a valid file descriptor was returned\n");
  if(fileDescriptor != -1){
		printf("Success: file created\nFile Descriptor: %d\n\n",fileDescriptor);
		return 0;
	}
  printf("Failure: file not created\n\n");
	return -1;
  
}