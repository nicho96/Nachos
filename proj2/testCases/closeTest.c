#include <stdio.h>
int main(int argc, char* argv[]){
  
  int fileDescriptor = creat("closeTestFile");
  int result = close(fileDescriptor);
  
	if(result == 0){
		printf("Success, file closed");
		return 0;
	}
	printf("Failure, file failed to close");
	return -1;
  
}