/*
 * expfilegen.c
 * Generates an Experiment File from a list of URLs.
 *
 * Future versions will include the ability to scan the experimental files to check for changes and alert the
 * ESS to this fact.
 *  Created on: 22 Jun 2015
 *      Author: mgohde
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define VERSIONCODE 1

void printHelp()
{
	printf("usage: expfilegen infilename outfilename [arguments]\n");
	printf("\t-h\tPrints this help message.\n");
	printf("\t-a\tAppend the experiment onto the end of another experiment file.\n");
	printf("\t-t\tSets experiment title.\n");
	printf("\t-d\tSets experiment description. Should be the name of a \n\t\tplaintext file.\n");
	printf("\t-p\tSets whether clients should post back to \"true\".\n");
	printf("\t-rs\tSets the response server name.\n");
	printf("\t-rp\tSets the response server port.\n");
}

/* This function handles a very common operation: checking whether we can use the
 * next element in an array without going out of bounds.
 */
int checkAdvance(int arrSize, int arrPos)
{
	return !(arrSize<=(arrPos+1));
}

char *loadDescription(FILE *inFile)
{
	char *description;
	int fileSize;

	fseek(inFile, 0, SEEK_END);
	fileSize=ftell(inFile);

	rewind(inFile);

	if(fileSize==0)
	{
		return NULL;
	}

	description=(char*) malloc(sizeof(char)*(fileSize+1));

	if(fread(description, sizeof(char), fileSize, inFile)==0)
	{
		return NULL;
	}

	description[fileSize]='\0'; /* Terminate the string.*/

	return description;
}

int countToLine(FILE *in)
{
	int curPos;
	int size;

	curPos=ftell(in);
	size=1;

	while(fgetc(in)!='\n'&&!feof(in))
	{
		size++;
	}

	fseek(in, curPos, SEEK_SET);

	return size;
}

char *readNextLine(FILE *in)
{
	char *lineBuffer;
	int buffrSize;

	buffrSize=countToLine(in);

	if(buffrSize==1)
	{
		return NULL; /* The file is probably done with. */
	}

	lineBuffer=(char*) malloc(sizeof(char)*(buffrSize));

	fread(lineBuffer, sizeof(char), buffrSize, in);

	lineBuffer[buffrSize-1]='\0'; /*This should eliminate the newline/EOF if there is one.*/

	return lineBuffer;
}

/* This generates the body of an experiment so it requires other functions to make it useful.
 */
void genBody(FILE *in, FILE *out, char *title, char *serverName, char *serverPort, char *description, char shouldPostBack)
{
	char *nextLine;

	fprintf(out, "\t<experiment>\n");

	if(title!=NULL)
	{
		fprintf(out, "\t\t<title>%s</title>\n", title);
	}

	if(serverName!=NULL)
	{
		fprintf(out, "\t\t<resserver>%s</resserver>\n", serverName);
	}

	if(serverPort!=NULL)
	{
		fprintf(out, "\t\t<resport>%s</resport>\n", serverPort);
	}

	if(description!=NULL)
	{
		fprintf(out, "\t\t<description>%s</description>\n", description);
	}

	if(shouldPostBack)
	{
		fprintf(out, "\t\t<report>true</report>\n");
	}

	else
	{
		fprintf(out, "\t\t<report>false</report>\n");
	}

	/* Now start with the actual dataset portion: */

	fprintf(out, "\t\t<data>\n");

	while((nextLine=readNextLine(in))!=NULL)
	{
		fprintf(out, "\t\t\t<url>%s</url>\n", nextLine);
		free(nextLine); /* Prevent leaks. */
	}

	fprintf(out, "\t\t</data>\n");
	fprintf(out, "\t</experiment>\n");
}

void newFile(char *inFileName, char *outFileName, char *title, char *serverName, char *serverPort, char *description, char shouldPostBack)
{
	FILE *inFile;
	FILE *outFile;

	inFile=NULL;
	outFile=NULL;

	if(inFileName!=NULL)
	{
		inFile=fopen(inFileName, "rb");
	}

	if(outFileName!=NULL)
	{
		outFile=fopen(outFileName, "w");
	}

	if(inFile==NULL||outFile==NULL)
	{
		printf("Error: Not all filenames specified!\n");
		exit(2); /*The OS should hopefully free up all of the RAM I've allocated so far.*/
	}

	fprintf(outFile, "<rsse version=\"%d\">\n", VERSIONCODE);
	genBody(inFile, outFile, title, serverName, serverPort, description, shouldPostBack);
	fprintf(outFile, "</rsse>");

	fclose(inFile);
	fclose(outFile);

	printf("Wrote \"%s\"\n", outFileName);
}

#define CLOSING_TAG "</rsse>"

char checkIfClosingTag(FILE *f)
{
	int startPos;
	char c[]=CLOSING_TAG;
	char buffr[80]; /* This is likely the least safe bit of code in the program so far.*/

	startPos=ftell(f);
	fscanf(f, "%s", buffr);
	fseek(f, startPos, SEEK_SET);

	return strcmp(buffr, c)==0;
}

#ifdef _WIN32
	#define CMD1 "copy"
	#define CMD2 "del"
#else
	#define CMD1 "cp"
	#define CMD2 "rm"
#endif

void appendFile(char *inFileName, char *outFileName, char *title, char *serverName, char *serverPort, char *description, char shouldPostBack)
{
	FILE *inFile;
	FILE *outFile;
	FILE *appendFile;
	char *appendFileName;
	char *c;
	char *buffr;

	inFile=NULL;
	outFile=NULL;

	appendFileName=(char*) malloc(sizeof(char)*(strlen(outFileName)+4));
	sprintf(appendFileName, "%s.tmp", outFileName);

	if(inFileName!=NULL)
	{
		inFile=fopen(inFileName, "rb");
	}

	if(outFileName!=NULL)
	{
		outFile=fopen(outFileName, "rb");
	}

	appendFile=fopen(appendFileName, "w");

	if(inFile==NULL||outFile==NULL)
	{
		printf("Error: Not all filenames specified!\n");
		exit(2); /*The OS should hopefully free up all of the RAM I've allocated so far.*/
	}

	/* Copy the remainder of the old file verbatim.*/
	while(!checkIfClosingTag(outFile))
	{
		c=readNextLine(outFile);
		if(c==NULL)
		{
			break;
		}

		printf("c=%s\n", c);
		fprintf(appendFile, "%s\n", c);
		free(c);
	}

	genBody(inFile, appendFile, title, serverName, serverPort, description, shouldPostBack);
	fprintf(appendFile, "</rsse>");

	buffr=(char*) malloc(sizeof(char)*(5+strlen(outFileName)));

	fclose(inFile);
	fclose(outFile);
	fclose(appendFile);

	/* Copy over the new file.*/
	sprintf(buffr, "%s %s %s", CMD1, appendFileName, outFileName);
	printf("%s\n", buffr);
	system(buffr);
	free(buffr);

	/* Remove the old one.*/
	buffr=(char*) malloc(sizeof(char)*(5+strlen(outFileName)));
	sprintf(buffr, "%s %s", CMD2, appendFileName);
	printf("%s\n", buffr);
	system(buffr);


	free(buffr);
	free(appendFileName);

	printf("Appended \"%s\"\n", outFileName);
}

int main(int argc, char **argv)
{
	char *fileNames[2];
	char *descriptionFileName, *title, *serverName;
	char *serverPort;
	char *description;
	FILE *descriptionFile;
	int i, fNamePos;

	char clientsShouldPostBack;
	char append;

	if(argc<3) //Minimum number required
	{
		/* Print a help message. */
		printHelp();
		return 1;
	}

	else
	{
		fileNames[0]=NULL;
		fileNames[1]=NULL;
		descriptionFileName=NULL;
		title=NULL;
		clientsShouldPostBack=0;
		fNamePos=0;
		append=0;


		for(i=1;i<argc;i++)
		{
			/* Check if it's a flag: */
			if(argv[i][0]=='-')
			{
				/* Determine its type: */
				if(argv[i][1]=='h')
				{
					printHelp();
					return 1;
				}

				else if(argv[i][1]=='d')
				{
					if(checkAdvance(argc, i))
					{
						i++;
						descriptionFileName=argv[i];
					}

					else
					{
						printf("Experiment description file name not specified!\n");
						return 2;
					}
				}

				else if(argv[i][1]=='p')
				{
					clientsShouldPostBack=1;
				}

				else if(argv[i][1]=='t')
				{
					if(checkAdvance(argc, i))
					{
						i++;
						title=argv[i];
					}

					else
					{
						printf("Experiment title not specified!\n");
						return 2;
					}
				}

				else if(argv[i][1]=='a')
				{
					append=1;
				}

				else if(argv[i][1]=='r')
				{
					if(checkAdvance(argc, i))
					{
						if(argv[i][2]=='s')
						{
							serverName=argv[i+1];
						}

						else if(argv[i][2]=='p')
						{
							serverPort=argv[i+1];
						}

						else
						{
							printf("Error in argument: %s\n", argv[i]);
							return 2;
						}

						i++;
					}

					else
					{
						printf("Name/port not specified after server config param.\n");
						return 2;
					}
				}
			}

			else /* Start trying to fill in those filenames: */
			{
				if(fNamePos<2)
				{
					fileNames[fNamePos]=argv[i];
					fNamePos++;
				}
			}
		}

		/*
		 * With that over with, start the thing:
		 */
		descriptionFile=fopen(descriptionFileName, "r");
		description=loadDescription(descriptionFile);
		fclose(descriptionFile);

		if(append)
		{
			appendFile(fileNames[0], fileNames[1], title, serverName, serverPort, description, clientsShouldPostBack);
		}

		else
		{
			newFile(fileNames[0], fileNames[1], title, serverName, serverPort, description, clientsShouldPostBack);
		}

		if(description!=NULL)
		{
			free(description);
		}
	}

	return 0;
}
