package prog12;

import java.util.List;
import java.io.*;
import java.util.*;
import prog06.ArrayQueue;

public class Binge implements SearchEngine{
    
	HardDisk<PageFile> pageDisk = new HardDisk<PageFile>();
	PageTrie URLToIndex = new PageTrie();
	int refCount;
	
	//Put a HardDisk variable wordDisk into Binge ... what is T???
	HardDisk<List<Long>> wordDisk = new HardDisk<List<Long>>();
	//Create a wordToIndex table which maps a word to its index
	WordTable wordToIndex = new WordTable();
	
	//indexing a page... storing a page & we are given the URL first
	public Long indexPage (String URL) {
		
		//get the index of the new file from pageDisk
		Long index = pageDisk.newFile();
		//Create a new PageFile using the index
    	PageFile pageFile = new PageFile(index,URL);
    	//storing the PageFile in the PageDisk
    	pageDisk.put(index, pageFile);
    	//Tell the trie to map the URL to that index
    	URLToIndex.put(URL,index);
    	
    	System.out.println("indexing page " + pageFile);
    	
    	return index;
    }
	
	/* Gather info from all web pages reachable from URLs in startingURLs. */
    public void gather (Browser browser, List<String> startingURLs) {
    	//Create a queue of page indices with ArrayDequeue
    	Queue<Long> queueIndices = new ArrayQueue<Long>();
    	System.out.println("gather " + startingURLs);
    	
    	//For each URL in startingURLs (parameter)
    	for(String url : startingURLs) {
    		//check if it has been indexed already
    		if(URLToIndex.containsKey(url)) {
    			continue;
    		}
    		//if not, index it and call the indexPage(URL) Function
    		else {
    			Long index = indexPage(url);
    			queueIndices.offer(index);
    		}
    	}
    	
    	Long pageIndex;
    	
    	//while the queue is not empty...
    	while(!queueIndices.isEmpty()) {
    		System.out.println("queue " + queueIndices);
    		pageIndex = queueIndices.poll();
    		System.out.println("dequeued " + pageDisk.get(pageIndex));
 
    		//get the page file from page disk (it is the URL of that file)
    		String pageFile = pageDisk.get(pageIndex).url;
    		
    		//if we can load URL into the browser successfully (browser.loadPage(URL of that file))
    		if(browser.loadPage(pageFile)) {
    			//make the list of URLs to go through in for each loop
        		List<String> urls = browser.getURLs();
        		System.out.println("urls " + urls);
        		Set<Long> pageSet = new HashSet<Long>();
    			//for each URL
    			for(String url: urls) {
    				//whatever indexes that haven't been indexed yet
    				if(!URLToIndex.containsKey(url)) {
    					//index the URL
        				Long index = indexPage(url);
        				//add that URL's index to the queue
        				queueIndices.offer(index);
        				pageSet.add(index);
        			} else {
        				pageSet.add(URLToIndex.get(url));
        			}
    			}
    			//put all the page indices into a Set first
    			for(Long element: pageSet) {
    					pageDisk.get(element).incRefCount();
    					System.out.println("inc ref " + pageDisk.get(element));
    			}
    			
    			
            	//step 9!!
            	List<String> words = browser.getWords();
            	System.out.println("words " + words);
            	for(String word : words) {
            		Long indexWord;
            		if(wordToIndex.containsKey(word)) {
            			indexWord = wordToIndex.get(word);
            		}
            		else {
            			indexWord = indexWord(word);
            		}
            		
            		List<Long> listWords = wordDisk.get(indexWord);
            		if(listWords.size() == 0 || listWords.get(listWords.size() - 1) != pageIndex) {
            			listWords.add(pageIndex);
            			System.out.println("add page " + indexWord + "(" + word + ")" + listWords);
            		}
            	}
    		}
    	}
    	
    	URLToIndex.write(pageDisk);
    	wordToIndex.write(wordDisk);
    	System.out.println("pageDisk" + "\n" + pageDisk);
    	System.out.println("urlToIndex " + "\n" + URLToIndex);
    	System.out.println("wordDisk " + "\n" + wordDisk);
    	System.out.println("wordToIndex " + "\n" + wordToIndex);
    }
    
    public Long indexWord (String word) {
    	//we need to know the list of web pages that have this word
    	//we want a list of web page indices with the word in it
    	
    	//get the index of the new file from wordDisk
    	Long index = wordDisk.newFile();
    	//Create a new references list (to use in wordDisk)
    	List<Long> references = new ArrayList<Long>();
    	//storing the reference list in the wordDisk
    	wordDisk.put(index, references);
    	//Tell the trie to map the word to that index
    	wordToIndex.put(word,index);
    	
    	System.out.println("indexing word " + index + "(" + word + ")" + references);
    	return index;
    }
    
    /* Search for up to numResults pages containing all keyWords and
     * return them in an array in order of decreasing importance
     * (number of references). */
    public String[] search (List<String> keyWords, int numResults) {
    	return new String[0];
    }
}