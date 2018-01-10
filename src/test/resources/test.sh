for file in `find . -name "*.json"`
do
    echo `cat $file | grep \"category\":`
    echo `curl -X POST -H "Content-Type: application/json" -d @$file http://localhost:8080/ad` > "$file".html
    if command -v xdg-open
    then
       xdg-open "$file".html
    else
       open "$file".html    
    fi
done
