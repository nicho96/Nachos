#include "stdio.h"
int main(int argc, char* argv[]){
  printf("----TESTING UNLINK----\n");
  
  printf("Creating a file called \"unlinkTestFile\"\n");
  creat("unlinkTestFile");
  
  printf("Trying to unlink \"unlinkTestFile\"\n");
  int result = unlink("unlinkTestFile");
	
  printf("Checking whether file was succesfully unlinked\n");
  if(result == 0){
		printf("Success: file has been unlinked\n\n");
		return 0;
	}
	printf("Failure: file has failed to unlink\n\n");
	return -1;
  
}