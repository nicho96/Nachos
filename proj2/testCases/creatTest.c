#include <stdio.h>
int main(int argc, char* argv[]){
  
  int fileDescriptor = creat("creatTestFile");
  
  if(fileDescriptor != -1){
		printf("Success: file created\n");
		return 0;
	}
  printf("Failure: file not created\n");
	return -1;
  
}