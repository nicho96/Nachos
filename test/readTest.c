#include "stdio.h"
int main(int argc, char* argv[]){
  printf("----TESTING READ----\n");
  
  printf("Creating a file called \"readTestFile\"\n");
  int fileDescriptor = creat("readTestFile");
	write(fileDescriptor, "Hello!", 6);
  
  char* buffer;
  printf("Trying to read from the test file\n");
	int bytesRead = read(fileDescriptor, buffer, 6);
  
  
  printf("Checking whether the char bytes have been succesfully read\n");
	if(bytesRead != -1){
		printf("Success: input \"");
    int i = 0;
    while(i < 6){
      printf("%c",buffer[i]);
      i++;
    }
    printf("\" has been read\nBytes read: %d\n\n", bytesRead);
		return 0;
	}
  printf("Failure: file was not read\n\n");
	return -1;
  
}
