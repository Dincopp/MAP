package SocialNetwork.repository.util;

public class Pageable {
    private int PageSize;
    private int PageNumber;

    public Pageable(int PageNumber,int PageSize){
        this.PageNumber=PageNumber;
        this.PageSize=PageSize;
    }

    public int getPageSize(){
        return PageSize;
    }

    public int getPageNumber(){
        return PageNumber;
    }
}
