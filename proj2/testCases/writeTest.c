#include <stdio.h>
int main(int argc, char* argv[]){
  
  int fileDescriptor = creat("writeTestFile");
  char* inputText = "Hello!";
  int length = 6;
  
	int bytesWritten = write(fileDescriptor, inputText, length);
  
	if(bytesWritten != -1){
		printf("Success: %d have been written\n", bytesWritten);
		return 0;
	}
  printf("Failure: file not written to\n");
	return -1;
  
}