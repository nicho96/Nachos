#include <stdio.h>
int main(int argc, char* argv[]){
  
  creat("creatTestFile");
  int result = unlink("creatTestFile");
	
  if(result == 0){
		printf("Success, file has been unlinked");
		return 0;
	}
	printf("Failure, file has failed to unlink");
	return -1;
  
}