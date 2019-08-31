package org.superbiz.moviefun.moviesapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class MovieServlet extends HttpServlet {

    private static final long serialVersionUID = -5832176047021911038L;

    @Value("${movies.pagesize}")
    public int pageSize;

    private MoviesClient moviesClient;

    public MovieServlet(MoviesClient moviesClient) {
        this.moviesClient = moviesClient;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("Add".equals(action)) {

            String title = request.getParameter("title");
            String director = request.getParameter("director");
            String genre = request.getParameter("genre");
            int rating = Integer.parseInt(request.getParameter("rating"));
            int year = Integer.parseInt(request.getParameter("year"));

            MovieInfo movie = new MovieInfo(null, title, director, genre, rating, year);

            moviesClient.addMovie(movie);
            response.sendRedirect("moviefun");
            return;

        } else if ("Remove".equals(action)) {

            String[] ids = request.getParameterValues("id");
            for (String id : ids) {
                moviesClient.deleteMovieId(new Long(id));
            }

            response.sendRedirect("moviefun");
            return;

        } else {
            String key = request.getParameter("key");
            String field = request.getParameter("field");

            int count = 0;

            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
                count = moviesClient.countAll();
                key = "";
                field = "";
            } else {
                count = moviesClient.count(field, key);
            }

            int page = 1;

            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (Exception e) {
            }

            int pageCount = (count / pageSize);
            if (pageCount == 0 || count % pageSize != 0) {
                pageCount++;
            }

            if (page < 1) {
                page = 1;
            }

            if (page > pageCount) {
                page = pageCount;
            }

            int start = (page - 1) * pageSize;
            List<MovieInfo> range;

            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
                range = moviesClient.findAll(start, pageSize);
            } else {
                range = moviesClient.findRange(field, key, start, pageSize);
            }

            int end = start + range.size();

            request.setAttribute("count", count);
            request.setAttribute("start", start + 1);
            request.setAttribute("end", end);
            request.setAttribute("page", page);
            request.setAttribute("pageCount", pageCount);
            request.setAttribute("movies", range);
            request.setAttribute("key", key);
            request.setAttribute("field", field);
        }

        request.getRequestDispatcher("WEB-INF/moviefun.jsp").forward(request, response);
    }

}