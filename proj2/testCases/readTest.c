#include <stdio.h>
int main(int argc, char* argv[]){
  
  int fileDescriptor = creat("readTestFile");
	write(fileDescriptor, "Hello!", 6);
  
  char* buffer;
	int bytesRead = read(fileDescriptor, buffer, 6);
  
	if(bytesRead != -1){
		printf("Success: file was read: ");
    int i = 0;
    while(i < 6){
      printf("%c", buffer[i]);
      i++;
    }
    printf("\n");
		return 0;
	}
  printf("Failure: file was not read\n");
	return -1;
  
}