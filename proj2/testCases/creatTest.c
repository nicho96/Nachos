#include "stdio.h"
int main(int argc, char* argv[]){
  printf("----TESTING CREATE----\n");
  
  printf("Creating a file called \"creatTestFile1\"\n");
  int fileDescriptor1 = creat("creatTestFile1");
  
  printf("Creating a file called \"creatTestFile2\"\n");
  int fileDescriptor2 = creat("creatTestFile2");
  
  printf("Checking whether valid file descriptors were returned\n");
  if(fileDescriptor1 != -1 && fileDescriptor2 != -1){
		printf("Success: files created\nFile Descriptor for createTestFile1: %d\nFile Descriptor for createTestFile2: %d\n\n",fileDescriptor1,fileDescriptor2);
		return 0;
	}
  printf("Failure: files not created\n\n");
	return -1;
  
}