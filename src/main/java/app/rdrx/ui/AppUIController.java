package app.rdrx.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppUIController {

    // serve all "/app/..." urls that are not specifically requesting a static file with the main app file.
    // oh, and the root. That too.
    @GetMapping(value={"/", "/app", "/app/{path:^(?!static$).*$}", "/app/{path:^(?!static$).*$}/**"})
    public String index() {
        return "/app/static/index.html";
    }

    // browsers like to blindly request this, so just for convenience's sake
    @GetMapping("favicon.ico")
    public String favicon(){
        return "/app/static/favicon.ico";
    }


}
