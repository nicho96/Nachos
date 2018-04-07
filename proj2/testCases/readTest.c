#include <stdio.h>
int main(int argc, char* argv[]){
  
  int fileDescriptor = creat("readTestFile");
	write(fileDescriptor, "Hello!", 6);
  
  void* buffer;
	int bytesRead = read(fileDescriptor, buffer, 6);
  
	if(bytesRead != -1){
		printf("Success: file was read\n", bytesWritten);
		return 0;
	}
  printf("Failure: file was not read\n");
	return -1;
  
}