#include "stdio.h"
int main(int argc, char* argv[]){
  printf("----TESTING WRITE----\n");
  
  printf("Creating a file called \"writeTestFile\"\n");
  int fileDescriptor = creat("writeTestFile");
  
  char* input = "Hello!";
  printf("Trying to write \"%s\" to the test file\n", input);
	int bytesWritten = write(fileDescriptor, input, 6);
  
  printf("Checking whether the char bytes have been succesfully written\n");
	if(bytesWritten != -1){
		printf("Success: input \"%s\" has been written\nBytes written: %d\n\n", input, bytesWritten);
		return 0;
	}
  printf("Failure: file not written to\n\n");
	return -1;
  
}