package br.com.marciocoelho.screenmatch.principal;

import br.com.marciocoelho.screenmatch.model.DadosSerie;
import br.com.marciocoelho.screenmatch.model.DadosTemporada;
import br.com.marciocoelho.screenmatch.model.Episodio;
import br.com.marciocoelho.screenmatch.service.ConsumoApi;
import br.com.marciocoelho.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String OMDB_API_URL = "https://www.omdbapi.com/?t=";
    private final String OMDB_API_KEY = "&apikey=501e6e7e";

    public void exibeMenu() {

        System.out.println("Digite o nome da série para busca:");
        var nomeSerie = leitura.nextLine();

        var json = consumoApi.obterDados( OMDB_API_URL
                + nomeSerie.replace(" ", "+") + OMDB_API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        if (dados.totalTemporadas() == null){
            System.out.println("Não foi identificado o numero de temporadas de " + nomeSerie);
            return;
        }

		List<DadosTemporada> temporadas = new ArrayList<>();
		for (int i = 1; i <= dados.totalTemporadas(); i++) {

			json = consumoApi.obterDados(OMDB_API_URL
                    + nomeSerie.replace(" ", "+") +"&season="
					+ i + "&apikey=501e6e7e");
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);

		}
		//temporadas.forEach(System.out::println);

//        for (int i = 0; i < dados.totalTemporadas(); i++) {
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size(); j++) {
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

        // lambdas:
        temporadas.forEach(
                t -> t.episodios().forEach(
                        e -> System.out.println(e.titulo())));


        // top 10 episodios da serie
//        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream())
//                //.toList();      // toList cria uma lista imutável
//                .collect(Collectors.toList());  // por isso nesse caso deve-se usar o .collect(Collectors.toList())
//        System.out.println("Top 10 episodios:");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
////                .peek(e -> System.out.println("Primeiro filtro - N/A " + e))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
////                .peek(e -> System.out.println("Ordenacao " + e))
//                .limit(10)
////                .peek(e -> System.out.println("Limit " + e))
//                .map(e->e.titulo().toUpperCase(Locale.ROOT))
////                .peek(e -> System.out.println("Mapeamento " + e))
//                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                    .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());
        episodios.forEach(System.out::println);
//
//        System.out.println("A partir de que ano voce deseja ver os episodios?");
//        var ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null
//                            &&  e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                " Episódio: " + e.getTitulo() +
//                                " Data de lançamento: " + e.getDataLancamento().format(dtf)
//                ));

//        System.out.println();
//        System.out.println();
//        System.out.println("Digite um trecho para busca de titulo:");
//        var trechoDoTitulo = leitura.nextLine();
//
//        Optional<Episodio> episodioEncontrado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase(Locale.ROOT).contains(trechoDoTitulo.toUpperCase(Locale.ROOT)))
//                .findFirst();
//        if (episodioEncontrado.isPresent()) {
//            System.out.println("Episodio encontrado!");
//            System.out.println("Temporada: " + episodioEncontrado.get().getTemporada());
//            System.out.println("Episodio: " + episodioEncontrado.get().getTitulo());
//        } else {
//            System.out.println("Episodio nao encontrado...");
//        }

//        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
//                .filter(e->e.getAvaliacao()>0.0)
//                .collect(Collectors.groupingBy(Episodio::getTemporada,
//                        Collectors.averagingDouble(Episodio::getAvaliacao)));
//        System.out.println(avaliacoesPorTemporada);


        // DoubleSummaryStatistics - tipo de retorno: {count=69,
        // sum=604,200000, min=4,000000, average=8,756522, max=9,900000}
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e->e.getAvaliacao()>0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println(est);

        System.out.println("Media: " + est.getAverage());
        System.out.println("Pior episodio: " + est.getMin());
        System.out.println("Melhor episodio: " + est.getMax());
        System.out.println("Qtd episodios: " + est.getCount());

    }

}
