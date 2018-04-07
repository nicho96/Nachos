#include "stdio.h"
int main(int argc, char* argv[]){
  printf("----TESTING CLOSE----\n");
  
  printf("Creating a file called \"closeTestFile\"\n");
  int fileDescriptor = creat("closeTestFile");
  
  printf("Trying to close \"closeTestFile\"\n");
  int result = close(fileDescriptor);
  
  printf("Checking whether file was succesfully closed\n");
	if(result == 0){
		printf("Success: file has been closed\n\n");
		return 0;
	}
	printf("Failure: file has failed to close\n\n");
	return -1;
  
}