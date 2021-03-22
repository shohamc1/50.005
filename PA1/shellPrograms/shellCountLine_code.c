#include "shellPrograms.h"

/*
Count the number of lines in a file 
*/
int shellCountLine_code(char **args)
{
    /** TASK 6  **/
    FILE *fp;
    char *line = NULL;
    int count = 0;
    size_t len = 0;
    ssize_t nread;

    // ATTENTION: you need to implement this function from scratch and not to utilize other system program to do this
    // 1. Given char** args, open file in READ mode based on the filename given in args[1] using fopen()
    fp = fopen(args[1], "r");

    // 2. Check if file exists by ensuring that the FILE* fp returned by fopen() is not NULL
    if (fp == NULL)
    {
        return 1;
    }

    // 3. Read the file line by line by using getline(&buffer, &size, fp)
    // 4. Loop, as long as getline() does not return -1, keeps reading and increment the count
    while ((nread = getline(&line, &len, fp)) != -1)
    {
        count++;
    }

    // 6. Close the FILE*
    fclose(fp);

    // 7. Print out how many lines are there in this particular filename
    printf("There are %d lines in %s.\n", count, args[1]);

    // 8. Return 1, to exit program
    return 1;
}

int main(int argc, char **args)
{
    return shellCountLine_code(args);
}